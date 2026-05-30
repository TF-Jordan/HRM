package yowyob.comops.api.hrm.adapter.in.web;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.CreateMedicalCertificateCommand;
import yowyob.comops.api.hrm.application.port.in.CreateMedicalVisitCommand;
import yowyob.comops.api.hrm.application.port.in.ManageMedicalUseCase;
import yowyob.comops.api.hrm.domain.model.AptitudeResult;
import yowyob.comops.api.hrm.domain.model.MedicalCertificate;
import yowyob.comops.api.hrm.domain.model.MedicalVisit;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/hrm/medical")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class MedicalController {

    private final ManageMedicalUseCase medicalUseCase;

    public MedicalController(ManageMedicalUseCase medicalUseCase) {
        this.medicalUseCase = medicalUseCase;
    }

    @PostMapping("/visits")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:medical:create')")
    public Mono<ResponseEntity<ApiResponse<MedicalVisitResponse>>> createVisit(
            @Valid @RequestBody Mono<CreateMedicalVisitRequest> requestMono) {
        return requestMono.map(CreateMedicalVisitRequest::toCommand)
                .flatMap(medicalUseCase::createVisit)
                .map(MedicalVisitResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Medical visit created.")));
    }

    @GetMapping("/visits/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:medical:read')")
    public Mono<ResponseEntity<ApiResponse<MedicalVisitResponse>>> getVisit(@PathVariable UUID id) {
        return medicalUseCase.getVisit(id).map(MedicalVisitResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Medical visit fetched.")));
    }

    @GetMapping("/employees/{employeeId}/visits")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:medical:read')")
    public Mono<ResponseEntity<ApiResponse<List<MedicalVisitResponse>>>> listVisitsByEmployee(
            @PathVariable UUID employeeId) {
        return medicalUseCase.listVisitsByEmployee(employeeId)
                .map(MedicalVisitResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Medical visits fetched.")));
    }

    @GetMapping("/visits")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:medical:read')")
    public Mono<ResponseEntity<ApiResponse<List<MedicalVisitResponse>>>> listVisits(
            @RequestParam UUID organizationId) {
        return medicalUseCase.listVisits(organizationId)
                .map(MedicalVisitResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Medical visits fetched.")));
    }

    @PostMapping("/certificates")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:medical:create')")
    public Mono<ResponseEntity<ApiResponse<MedicalCertificateResponse>>> createCertificate(
            @Valid @RequestBody Mono<CreateMedicalCertificateRequest> requestMono) {
        return requestMono.map(CreateMedicalCertificateRequest::toCommand)
                .flatMap(medicalUseCase::createCertificate)
                .map(MedicalCertificateResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Medical certificate created.")));
    }

    @GetMapping("/certificates/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:medical:read')")
    public Mono<ResponseEntity<ApiResponse<MedicalCertificateResponse>>> getCertificate(@PathVariable UUID id) {
        return medicalUseCase.getCertificate(id).map(MedicalCertificateResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Medical certificate fetched.")));
    }

    @GetMapping("/employees/{employeeId}/certificates")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:medical:read')")
    public Mono<ResponseEntity<ApiResponse<List<MedicalCertificateResponse>>>> listCertificatesByEmployee(
            @PathVariable UUID employeeId) {
        return medicalUseCase.listCertificatesByEmployee(employeeId)
                .map(MedicalCertificateResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Medical certificates fetched.")));
    }

    @GetMapping("/certificates")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:medical:read')")
    public Mono<ResponseEntity<ApiResponse<List<MedicalCertificateResponse>>>> listCertificates(
            @RequestParam UUID organizationId) {
        return medicalUseCase.listCertificates(organizationId)
                .map(MedicalCertificateResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Medical certificates fetched.")));
    }

    public record CreateMedicalVisitRequest(UUID employeeId, LocalDate dateVisite, String medecin,
            AptitudeResult resultatAptitude, String restrictions, LocalDate prochaineEcheance,
            UUID certificatFileId) {
        CreateMedicalVisitCommand toCommand() {
            return new CreateMedicalVisitCommand(employeeId, dateVisite, medecin, resultatAptitude,
                    restrictions, prochaineEcheance, certificatFileId);
        }
    }

    public record MedicalVisitResponse(UUID id, UUID employeeId, LocalDate dateVisite, String medecin,
            String resultatAptitude, String restrictions, LocalDate prochaineEcheance, UUID certificatFileId) {
        static MedicalVisitResponse from(MedicalVisit v) {
            return new MedicalVisitResponse(v.id(), v.employeeId(), v.dateVisite(), v.medecin(),
                    v.resultatAptitude().name(), v.restrictions(), v.prochaineEcheance(), v.certificatFileId());
        }
    }

    public record CreateMedicalCertificateRequest(UUID employeeId, String typeCertificat,
            LocalDate dateEmission, LocalDate dateExpiration, String statut, UUID fichierId) {
        CreateMedicalCertificateCommand toCommand() {
            return new CreateMedicalCertificateCommand(employeeId, typeCertificat, dateEmission,
                    dateExpiration, statut, fichierId);
        }
    }

    public record MedicalCertificateResponse(UUID id, UUID employeeId, String typeCertificat,
            LocalDate dateEmission, LocalDate dateExpiration, String statut, UUID fichierId) {
        static MedicalCertificateResponse from(MedicalCertificate c) {
            return new MedicalCertificateResponse(c.id(), c.employeeId(), c.typeCertificat(),
                    c.dateEmission(), c.dateExpiration(), c.statut(), c.fichierId());
        }
    }
}
