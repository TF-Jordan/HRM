package yowyob.comops.api.hrm.adapter.in.web;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.*;
import yowyob.comops.api.hrm.domain.model.*;

import jakarta.validation.Valid;
import java.time.Instant;
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
@RequestMapping("/api/v1/hrm")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class RecruitmentController {

    private final ManageRecruitmentUseCase recruitmentUseCase;

    public RecruitmentController(ManageRecruitmentUseCase recruitmentUseCase) {
        this.recruitmentUseCase = recruitmentUseCase;
    }

    // --- Job Offers ---

    @PostMapping("/job-offers")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:create')")
    public Mono<ResponseEntity<ApiResponse<JobOfferResponse>>> createJobOffer(
            @Valid @RequestBody Mono<CreateJobOfferRequest> requestMono) {
        return requestMono.map(CreateJobOfferRequest::toCommand)
                .flatMap(recruitmentUseCase::createJobOffer)
                .map(JobOfferResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Job offer created.")));
    }

    @PutMapping("/job-offers/{id}/publish")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:manage')")
    public Mono<ResponseEntity<ApiResponse<JobOfferResponse>>> publishJobOffer(@PathVariable UUID id) {
        return recruitmentUseCase.publishJobOffer(id).map(JobOfferResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Job offer published.")));
    }

    @PutMapping("/job-offers/{id}/close")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:manage')")
    public Mono<ResponseEntity<ApiResponse<JobOfferResponse>>> closeJobOffer(@PathVariable UUID id) {
        return recruitmentUseCase.closeJobOffer(id).map(JobOfferResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Job offer closed.")));
    }

    @GetMapping("/job-offers/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:read')")
    public Mono<ResponseEntity<ApiResponse<JobOfferResponse>>> getJobOffer(@PathVariable UUID id) {
        return recruitmentUseCase.getJobOffer(id).map(JobOfferResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Job offer fetched.")));
    }

    @GetMapping("/job-offers")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:read')")
    public Mono<ResponseEntity<ApiResponse<List<JobOfferResponse>>>> listJobOffers(@RequestParam UUID organizationId) {
        return recruitmentUseCase.listJobOffersByOrganization(organizationId)
                .map(JobOfferResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Job offers fetched.")));
    }

    // --- Applications ---

    @PostMapping("/applications")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:create')")
    public Mono<ResponseEntity<ApiResponse<ApplicationResponse>>> createApplication(
            @Valid @RequestBody Mono<CreateApplicationRequest> requestMono) {
        return requestMono.map(CreateApplicationRequest::toCommand)
                .flatMap(recruitmentUseCase::createApplication)
                .map(ApplicationResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Application created.")));
    }

    @PutMapping("/applications/{id}/shortlist")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:manage')")
    public Mono<ResponseEntity<ApiResponse<ApplicationResponse>>> shortlistApplication(@PathVariable UUID id) {
        return recruitmentUseCase.shortlistApplication(id).map(ApplicationResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Application shortlisted.")));
    }

    @PutMapping("/applications/{id}/interview")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:manage')")
    public Mono<ResponseEntity<ApiResponse<ApplicationResponse>>> interviewApplication(@PathVariable UUID id) {
        return recruitmentUseCase.interviewApplication(id).map(ApplicationResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Application moved to interviewing.")));
    }

    @PutMapping("/applications/{id}/offer")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:manage')")
    public Mono<ResponseEntity<ApiResponse<ApplicationResponse>>> offerApplication(@PathVariable UUID id) {
        return recruitmentUseCase.offerApplication(id).map(ApplicationResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Offer extended.")));
    }

    @PutMapping("/applications/{id}/reject")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:manage')")
    public Mono<ResponseEntity<ApiResponse<ApplicationResponse>>> rejectApplication(@PathVariable UUID id) {
        return recruitmentUseCase.rejectApplication(id).map(ApplicationResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Application rejected.")));
    }

    @PutMapping("/applications/{id}/hire")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:manage')")
    public Mono<ResponseEntity<ApiResponse<ApplicationResponse>>> hireApplication(@PathVariable UUID id) {
        return recruitmentUseCase.hireApplication(id).map(ApplicationResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Candidate hired.")));
    }

    /**
     * End-to-end conversion: provisions an Actor from the candidate identity,
     * creates the HRM Employee (matricule generated server-side) with an
     * active contract, and marks the application HIRED. Requires both the
     * recruitment manage and the employee create permissions because it
     * touches both aggregates.
     */
    @PostMapping("/applications/{id}/convert-to-employee")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:manage') and "
            + "@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:create')")
    public Mono<ResponseEntity<ApiResponse<EmployeeResponse>>> convertApplicationToEmployee(
            @PathVariable UUID id, @Valid @RequestBody Mono<ConvertApplicationRequest> requestMono) {
        return requestMono.map(r -> r.toCommand(id))
                .flatMap(recruitmentUseCase::convertApplicationToEmployee)
                .map(EmployeeResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Candidate hired and employee provisioned.")));
    }

    @GetMapping("/applications/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:read')")
    public Mono<ResponseEntity<ApiResponse<ApplicationResponse>>> getApplication(@PathVariable UUID id) {
        return recruitmentUseCase.getApplication(id).map(ApplicationResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Application fetched.")));
    }

    @GetMapping("/job-offers/{jobOfferId}/applications")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:read')")
    public Mono<ResponseEntity<ApiResponse<List<ApplicationResponse>>>> listApplications(@PathVariable UUID jobOfferId) {
        return recruitmentUseCase.listApplicationsByJobOffer(jobOfferId)
                .map(ApplicationResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Applications fetched.")));
    }

    // --- Interviews ---

    @PostMapping("/interviews")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:manage')")
    public Mono<ResponseEntity<ApiResponse<InterviewResponse>>> scheduleInterview(
            @Valid @RequestBody Mono<ScheduleInterviewRequest> requestMono) {
        return requestMono.map(ScheduleInterviewRequest::toCommand)
                .flatMap(recruitmentUseCase::scheduleInterview)
                .map(InterviewResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Interview scheduled.")));
    }

    @PutMapping("/interviews/{id}/complete")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:manage')")
    public Mono<ResponseEntity<ApiResponse<InterviewResponse>>> completeInterview(
            @PathVariable UUID id, @Valid @RequestBody Mono<CompleteInterviewRequest> requestMono) {
        return requestMono.flatMap(r -> recruitmentUseCase.completeInterview(id, r.notes(), r.resultat()))
                .map(InterviewResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Interview completed.")));
    }

    @GetMapping("/applications/{applicationId}/interviews")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:recruitment:read')")
    public Mono<ResponseEntity<ApiResponse<List<InterviewResponse>>>> listInterviews(@PathVariable UUID applicationId) {
        return recruitmentUseCase.listInterviewsByApplication(applicationId)
                .map(InterviewResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Interviews fetched.")));
    }

    // --- Onboarding Tasks ---

    @PostMapping("/onboarding-tasks")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:onboarding:create')")
    public Mono<ResponseEntity<ApiResponse<OnboardingTaskResponse>>> createOnboardingTask(
            @Valid @RequestBody Mono<CreateOnboardingTaskRequest> requestMono) {
        return requestMono.map(CreateOnboardingTaskRequest::toCommand)
                .flatMap(recruitmentUseCase::createOnboardingTask)
                .map(OnboardingTaskResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Onboarding task created.")));
    }

    @PutMapping("/onboarding-tasks/{id}/start")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:onboarding:manage')")
    public Mono<ResponseEntity<ApiResponse<OnboardingTaskResponse>>> startOnboardingTask(@PathVariable UUID id) {
        return recruitmentUseCase.startOnboardingTask(id).map(OnboardingTaskResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Onboarding task started.")));
    }

    @PutMapping("/onboarding-tasks/{id}/complete")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:onboarding:manage')")
    public Mono<ResponseEntity<ApiResponse<OnboardingTaskResponse>>> completeOnboardingTask(@PathVariable UUID id) {
        return recruitmentUseCase.completeOnboardingTask(id).map(OnboardingTaskResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Onboarding task completed.")));
    }

    @GetMapping("/onboarding-tasks/employee/{employeeId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:onboarding:read')")
    public Mono<ResponseEntity<ApiResponse<List<OnboardingTaskResponse>>>> listOnboardingTasks(@PathVariable UUID employeeId) {
        return recruitmentUseCase.listOnboardingTasksByEmployee(employeeId)
                .map(OnboardingTaskResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Onboarding tasks fetched.")));
    }

    // --- DTOs ---

    public record CreateJobOfferRequest(UUID agencyId, String poste, String departement, String localisation,
            String competencesRequises, LocalDate dateLimite, String packageSalarial) {
        CreateJobOfferCommand toCommand() {
            return new CreateJobOfferCommand(agencyId, poste, departement, localisation,
                    competencesRequises, dateLimite, packageSalarial);
        }
    }

    public record CreateApplicationRequest(UUID jobOfferId, String candidatNom, String candidatPrenom,
            String candidatEmail, String candidatTelephone, UUID cvFileId, UUID lettreMotivationFileId) {
        CreateApplicationCommand toCommand() {
            return new CreateApplicationCommand(jobOfferId, candidatNom, candidatPrenom,
                    candidatEmail, candidatTelephone, cvFileId, lettreMotivationFileId);
        }
    }

    public record ConvertApplicationRequest(
            UUID managerId, String numCnps, int categorie, String echelon,
            LocalDate dateEmbauche, String departmentCode, String modePaiement,
            String compteBancaire, String numMobileMoney, String operateurMm,
            String contractType, String position, LocalDate contractDateDebut, LocalDate contractDateFin,
            java.math.BigDecimal salaireBase, java.math.BigDecimal avantagesNature,
            Integer periodeEssai) {
        ConvertApplicationCommand toCommand(UUID applicationId) {
            return new ConvertApplicationCommand(applicationId, managerId, numCnps, categorie,
                    echelon, dateEmbauche, departmentCode, modePaiement, compteBancaire,
                    numMobileMoney, operateurMm, contractType, position, contractDateDebut, contractDateFin,
                    salaireBase, avantagesNature, periodeEssai);
        }
    }

    /** Minimal employee snapshot returned by the conversion endpoint. */
    public record EmployeeResponse(UUID id, UUID organizationId, UUID actorId, String matricule,
            int categorie, String echelon, LocalDate dateEmbauche, String status,
            String departmentCode, String actorDisplayName) {
        static EmployeeResponse from(yowyob.comops.api.hrm.domain.model.Employee e) {
            return new EmployeeResponse(e.id(), e.organizationId(), e.actorId(), e.matricule(),
                    e.categorie(), e.echelon(), e.dateEmbauche(), e.status().name(),
                    e.departmentCode(), e.actorDisplayName());
        }
    }

    public record ScheduleInterviewRequest(UUID applicationId, InterviewType type, Instant dateHeure,
            String lieu, UUID interviewerPartyId, String interviewerDisplayName) {
        ScheduleInterviewCommand toCommand() {
            return new ScheduleInterviewCommand(applicationId, type, dateHeure, lieu,
                    interviewerPartyId, interviewerDisplayName);
        }
    }

    public record CompleteInterviewRequest(String notes, InterviewResult resultat) {}

    public record CreateOnboardingTaskRequest(UUID employeeId, String titre, String description,
            UUID assignedToPartyId, LocalDate echeance) {
        CreateOnboardingTaskCommand toCommand() {
            return new CreateOnboardingTaskCommand(employeeId, titre, description, assignedToPartyId, echeance);
        }
    }

    public record JobOfferResponse(UUID id, UUID organizationId, UUID agencyId, String poste,
            String departement, String localisation, String competencesRequises,
            LocalDate dateLimite, String packageSalarial, String status) {
        static JobOfferResponse from(JobOffer o) {
            return new JobOfferResponse(o.id(), o.organizationId(), o.agencyId(), o.poste(),
                    o.departement(), o.localisation(), o.competencesRequises(), o.dateLimite(),
                    o.packageSalarial(), o.status().name());
        }
    }

    public record ApplicationResponse(UUID id, UUID jobOfferId, String candidatNom, String candidatPrenom,
            String candidatEmail, String candidatTelephone, UUID cvFileId,
            UUID lettreMotivationFileId, String status) {
        static ApplicationResponse from(Application a) {
            return new ApplicationResponse(a.id(), a.jobOfferId(), a.candidatNom(), a.candidatPrenom(),
                    a.candidatEmail(), a.candidatTelephone(), a.cvFileId(), a.lettreMotivationFileId(),
                    a.status().name());
        }
    }

    public record InterviewResponse(UUID id, UUID applicationId, String type, Instant dateHeure,
            String lieu, UUID interviewerPartyId, String interviewerDisplayName,
            String notes, String resultat) {
        static InterviewResponse from(Interview i) {
            return new InterviewResponse(i.id(), i.applicationId(), i.type().name(), i.dateHeure(),
                    i.lieu(), i.interviewerPartyId(), i.interviewerDisplayName(), i.notes(),
                    i.resultat().name());
        }
    }

    public record OnboardingTaskResponse(UUID id, UUID employeeId, String titre, String description,
            UUID assignedToPartyId, LocalDate echeance, String status) {
        static OnboardingTaskResponse from(OnboardingTask t) {
            return new OnboardingTaskResponse(t.id(), t.employeeId(), t.titre(), t.description(),
                    t.assignedToPartyId(), t.echeance(), t.status().name());
        }
    }
}
