package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.AddObjectiveCommand;
import yowyob.comops.api.hrm.application.port.in.CreateReviewCommand;
import yowyob.comops.api.hrm.application.port.in.ManageReviewUseCase;
import yowyob.comops.api.hrm.domain.model.PerformanceReview;
import yowyob.comops.api.hrm.domain.model.ReviewObjective;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/hrm/reviews")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class ReviewController {

    private final ManageReviewUseCase manageReviewUseCase;

    public ReviewController(ManageReviewUseCase manageReviewUseCase) {
        this.manageReviewUseCase = manageReviewUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:review:create')")
    public Mono<ResponseEntity<ApiResponse<ReviewResponse>>> createReview(
            @Valid @RequestBody Mono<CreateReviewRequest> requestMono) {
        return requestMono.map(CreateReviewRequest::toCommand)
                .flatMap(manageReviewUseCase::createReview)
                .map(ReviewResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Review created.")));
    }

    @PutMapping("/{reviewId}/submit")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:review:manage')")
    public Mono<ResponseEntity<ApiResponse<ReviewResponse>>> submitReview(
            @PathVariable UUID reviewId, @Valid @RequestBody Mono<SubmitReviewRequest> requestMono) {
        return requestMono.flatMap(r -> manageReviewUseCase.submitReview(reviewId, r.noteGlobale(), r.commentaires(), r.planAction()))
                .map(ReviewResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Review submitted.")));
    }

    @PutMapping("/{reviewId}/acknowledge")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:review:manage')")
    public Mono<ResponseEntity<ApiResponse<ReviewResponse>>> acknowledgeReview(@PathVariable UUID reviewId) {
        return manageReviewUseCase.acknowledgeReview(reviewId)
                .map(ReviewResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Review acknowledged.")));
    }

    @PutMapping("/{reviewId}/finalize")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:review:manage')")
    public Mono<ResponseEntity<ApiResponse<ReviewResponse>>> finalizeReview(@PathVariable UUID reviewId) {
        return manageReviewUseCase.finalizeReview(reviewId)
                .map(ReviewResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Review finalized.")));
    }

    @GetMapping("/{reviewId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:review:read')")
    public Mono<ResponseEntity<ApiResponse<ReviewResponse>>> getReview(@PathVariable UUID reviewId) {
        return manageReviewUseCase.getReview(reviewId)
                .map(ReviewResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Review fetched.")));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:review:read')")
    public Mono<ResponseEntity<ApiResponse<List<ReviewResponse>>>> listByEmployee(@PathVariable UUID employeeId) {
        return manageReviewUseCase.listByEmployee(employeeId)
                .map(ReviewResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Reviews fetched.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:review:read')")
    public Mono<ResponseEntity<ApiResponse<List<ReviewResponse>>>> listByOrganizationAndPeriode(
            @RequestParam UUID organizationId, @RequestParam String periode) {
        return manageReviewUseCase.listByOrganizationAndPeriode(organizationId, periode)
                .map(ReviewResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Reviews fetched.")));
    }

    @PostMapping("/{reviewId}/objectives")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:review:manage')")
    public Mono<ResponseEntity<ApiResponse<ObjectiveResponse>>> addObjective(
            @PathVariable UUID reviewId, @Valid @RequestBody Mono<AddObjectiveRequest> requestMono) {
        return requestMono.map(r -> new AddObjectiveCommand(reviewId, r.description(), r.poids()))
                .flatMap(manageReviewUseCase::addObjective)
                .map(ObjectiveResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Objective added.")));
    }

    @PutMapping("/objectives/{objectiveId}/evaluate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:review:manage')")
    public Mono<ResponseEntity<ApiResponse<ObjectiveResponse>>> evaluateObjective(
            @PathVariable UUID objectiveId, @Valid @RequestBody Mono<EvaluateObjectiveRequest> requestMono) {
        return requestMono.flatMap(r -> manageReviewUseCase.evaluateObjective(objectiveId, r.noteAtteinte(), r.commentaire()))
                .map(ObjectiveResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Objective evaluated.")));
    }

    @GetMapping("/{reviewId}/objectives")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:review:read')")
    public Mono<ResponseEntity<ApiResponse<List<ObjectiveResponse>>>> listObjectives(@PathVariable UUID reviewId) {
        return manageReviewUseCase.listObjectivesByReview(reviewId)
                .map(ObjectiveResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Objectives fetched.")));
    }

    public record CreateReviewRequest(UUID employeeId, UUID evaluateurPartyId,
            String evaluateurDisplayName, String periode) {
        CreateReviewCommand toCommand() {
            return new CreateReviewCommand(employeeId, evaluateurPartyId, evaluateurDisplayName, periode);
        }
    }

    public record SubmitReviewRequest(BigDecimal noteGlobale, String commentaires, String planAction) {}

    public record AddObjectiveRequest(String description, BigDecimal poids) {}

    public record EvaluateObjectiveRequest(BigDecimal noteAtteinte, String commentaire) {}

    public record ReviewResponse(UUID id, UUID organizationId, UUID employeeId, UUID evaluateurPartyId,
            String evaluateurDisplayName, String periode, BigDecimal noteGlobale,
            String commentaires, String planAction, String status) {
        static ReviewResponse from(PerformanceReview r) {
            return new ReviewResponse(r.id(), r.organizationId(), r.employeeId(), r.evaluateurPartyId(),
                    r.evaluateurDisplayName(), r.periode(), r.noteGlobale(), r.commentaires(),
                    r.planAction(), r.status().name());
        }
    }

    public record ObjectiveResponse(UUID id, UUID reviewId, String description, BigDecimal poids,
            BigDecimal noteAtteinte, String commentaire) {
        static ObjectiveResponse from(ReviewObjective o) {
            return new ObjectiveResponse(o.id(), o.reviewId(), o.description(), o.poids(),
                    o.noteAtteinte(), o.commentaire());
        }
    }
}
