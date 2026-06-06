package yowyob.comops.api.hrm.adapter.in.web;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.adapter.in.web.MedicalController.MedicalCertificateResponse;
import yowyob.comops.api.hrm.application.port.in.ManageMedicalUseCase;
import yowyob.comops.api.hrm.application.port.in.SubmitMyMedicalCertificateCommand;
import yowyob.comops.api.hrm.domain.EmployeeNotFoundException;

/**
 * Medical self-service: a worker uploads/files their OWN medical certificate. Guarded only by a
 * valid user context — the operation is scoped to the employee record linked to the caller's actor,
 * so a worker can never file a certificate for someone else. The certificate lands as SOUMIS,
 * pending review by occupational health / HR.
 */
@Profile("!test-memory")
@RestController("hrmMedicalSelfServiceController")
@RequestMapping("/api/v1/hrm/medical/me")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class MedicalSelfServiceController {

    private final ManageMedicalUseCase medicalUseCase;

    public MedicalSelfServiceController(ManageMedicalUseCase medicalUseCase) {
        this.medicalUseCase = medicalUseCase;
    }

    @PostMapping("/certificates")
    public Mono<ResponseEntity<ApiResponse<MedicalCertificateResponse>>> submitMyCertificate(
            @Valid @RequestBody Mono<SubmitMyCertificateRequest> requestMono) {
        return requestMono.map(SubmitMyCertificateRequest::toCommand)
                .flatMap(medicalUseCase::submitMyCertificate)
                .map(MedicalCertificateResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(r, "Medical certificate submitted.")));
    }

    public record SubmitMyCertificateRequest(
            @NotBlank String typeCertificat,
            @NotNull LocalDate dateEmission,
            @NotNull LocalDate dateExpiration,
            UUID fichierId) {
        SubmitMyMedicalCertificateCommand toCommand() {
            return new SubmitMyMedicalCertificateCommand(typeCertificat, dateEmission, dateExpiration, fichierId);
        }
    }

    @RestControllerAdvice(assignableTypes = MedicalSelfServiceController.class)
    static class SelfServiceExceptionHandler {

        @ExceptionHandler(EmployeeNotFoundException.class)
        ResponseEntity<ApiResponse<Void>> handleEmployeeNotFound(EmployeeNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(ex.getMessage(), "EMPLOYEE_NOT_FOUND"));
        }
    }
}
