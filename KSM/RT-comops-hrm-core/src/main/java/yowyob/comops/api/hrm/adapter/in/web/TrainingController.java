package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.EnrollTrainingCommand;
import yowyob.comops.api.hrm.application.port.in.ManageTrainingUseCase;
import yowyob.comops.api.hrm.application.port.in.PlanTrainingCommand;
import yowyob.comops.api.hrm.domain.model.Training;
import yowyob.comops.api.hrm.domain.model.TrainingEnrollment;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/hrm/trainings")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class TrainingController {

    private final ManageTrainingUseCase manageTrainingUseCase;

    public TrainingController(ManageTrainingUseCase manageTrainingUseCase) {
        this.manageTrainingUseCase = manageTrainingUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:training:create')")
    public Mono<ResponseEntity<ApiResponse<TrainingResponse>>> planTraining(
            @Valid @RequestBody Mono<PlanTrainingRequest> requestMono) {
        return requestMono.map(PlanTrainingRequest::toCommand)
                .flatMap(manageTrainingUseCase::planTraining)
                .map(TrainingResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Training planned.")));
    }

    @PutMapping("/{trainingId}/start")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:training:manage')")
    public Mono<ResponseEntity<ApiResponse<TrainingResponse>>> startTraining(@PathVariable UUID trainingId) {
        return manageTrainingUseCase.startTraining(trainingId)
                .map(TrainingResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Training started.")));
    }

    @PutMapping("/{trainingId}/complete")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:training:manage')")
    public Mono<ResponseEntity<ApiResponse<TrainingResponse>>> completeTraining(@PathVariable UUID trainingId) {
        return manageTrainingUseCase.completeTraining(trainingId)
                .map(TrainingResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Training completed.")));
    }

    @PutMapping("/{trainingId}/cancel")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:training:manage')")
    public Mono<ResponseEntity<ApiResponse<TrainingResponse>>> cancelTraining(@PathVariable UUID trainingId) {
        return manageTrainingUseCase.cancelTraining(trainingId)
                .map(TrainingResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Training cancelled.")));
    }

    @GetMapping("/{trainingId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:training:read')")
    public Mono<ResponseEntity<ApiResponse<TrainingResponse>>> getTraining(@PathVariable UUID trainingId) {
        return manageTrainingUseCase.getTraining(trainingId)
                .map(TrainingResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Training fetched.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:training:read')")
    public Mono<ResponseEntity<ApiResponse<List<TrainingResponse>>>> listByOrganization(
            @RequestParam UUID organizationId) {
        return manageTrainingUseCase.listByOrganization(organizationId)
                .map(TrainingResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Trainings fetched.")));
    }

    @PostMapping("/{trainingId}/enrollments")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:training:manage')")
    public Mono<ResponseEntity<ApiResponse<EnrollmentResponse>>> enrollEmployee(
            @PathVariable UUID trainingId, @Valid @RequestBody Mono<EnrollRequest> requestMono) {
        return requestMono.map(r -> new EnrollTrainingCommand(trainingId, r.employeeId()))
                .flatMap(manageTrainingUseCase::enrollEmployee)
                .map(EnrollmentResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Employee enrolled.")));
    }

    @PutMapping("/enrollments/{enrollmentId}/complete")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:training:manage')")
    public Mono<ResponseEntity<ApiResponse<EnrollmentResponse>>> completeEnrollment(
            @PathVariable UUID enrollmentId, @Valid @RequestBody Mono<CompleteEnrollmentRequest> requestMono) {
        return requestMono.flatMap(r -> manageTrainingUseCase.completeEnrollment(enrollmentId, r.note(), r.attestationId()))
                .map(EnrollmentResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Enrollment completed.")));
    }

    @PutMapping("/enrollments/{enrollmentId}/cancel")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:training:manage')")
    public Mono<ResponseEntity<ApiResponse<EnrollmentResponse>>> cancelEnrollment(@PathVariable UUID enrollmentId) {
        return manageTrainingUseCase.cancelEnrollment(enrollmentId)
                .map(EnrollmentResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Enrollment cancelled.")));
    }

    @GetMapping("/{trainingId}/enrollments")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:training:read')")
    public Mono<ResponseEntity<ApiResponse<List<EnrollmentResponse>>>> listEnrollmentsByTraining(
            @PathVariable UUID trainingId) {
        return manageTrainingUseCase.listEnrollmentsByTraining(trainingId)
                .map(EnrollmentResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Enrollments fetched.")));
    }

    @GetMapping("/enrollments/employee/{employeeId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:training:read')")
    public Mono<ResponseEntity<ApiResponse<List<EnrollmentResponse>>>> listEnrollmentsByEmployee(
            @PathVariable UUID employeeId) {
        return manageTrainingUseCase.listEnrollmentsByEmployee(employeeId)
                .map(EnrollmentResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Enrollments fetched.")));
    }

    public record PlanTrainingRequest(UUID agencyId, String intitule, String organisme,
            LocalDate dateDebut, LocalDate dateFin, BigDecimal cout, Integer nbPlaces, String lieu) {
        PlanTrainingCommand toCommand() {
            return new PlanTrainingCommand(agencyId, intitule, organisme, dateDebut, dateFin, cout, nbPlaces, lieu);
        }
    }

    public record EnrollRequest(UUID employeeId) {}

    public record CompleteEnrollmentRequest(BigDecimal note, UUID attestationId) {}

    public record TrainingResponse(UUID id, UUID organizationId, UUID agencyId, String intitule,
            String organisme, LocalDate dateDebut, LocalDate dateFin, BigDecimal cout,
            Integer nbPlaces, String lieu, String status) {
        static TrainingResponse from(Training t) {
            return new TrainingResponse(t.id(), t.organizationId(), t.agencyId(), t.intitule(),
                    t.organisme(), t.dateDebut(), t.dateFin(), t.cout(), t.nbPlaces(), t.lieu(),
                    t.status().name());
        }
    }

    public record EnrollmentResponse(UUID id, UUID trainingId, UUID employeeId, String status,
            BigDecimal noteEvaluation, UUID attestationFileId) {
        static EnrollmentResponse from(TrainingEnrollment e) {
            return new EnrollmentResponse(e.id(), e.trainingId(), e.employeeId(), e.status().name(),
                    e.noteEvaluation(), e.attestationFileId());
        }
    }
}
