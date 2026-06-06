package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.PerformanceReview;
import yowyob.comops.api.hrm.domain.model.ReviewObjective;

import java.math.BigDecimal;
import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageReviewUseCase {

    Mono<PerformanceReview> createReview(CreateReviewCommand command);

    Mono<PerformanceReview> submitReview(UUID reviewId, BigDecimal noteGlobale, String commentaires, String planAction);

    Mono<PerformanceReview> acknowledgeReview(UUID reviewId);

    /**
     * Self-service acknowledgement: the calling worker acknowledges their OWN review
     * ({@code SUBMITTED → ACKNOWLEDGED}). Resolves the employee from the caller's actor and rejects
     * the operation when the review does not belong to them.
     */
    Mono<PerformanceReview> acknowledgeMyReview(UUID reviewId);

    Mono<PerformanceReview> finalizeReview(UUID reviewId);

    Mono<PerformanceReview> getReview(UUID reviewId);

    Flux<PerformanceReview> listByEmployee(UUID employeeId);

    Flux<PerformanceReview> listByOrganizationAndPeriode(UUID organizationId, String periode);

    Mono<ReviewObjective> addObjective(AddObjectiveCommand command);

    Mono<ReviewObjective> evaluateObjective(UUID objectiveId, BigDecimal noteAtteinte, String commentaire);

    Flux<ReviewObjective> listObjectivesByReview(UUID reviewId);
}
