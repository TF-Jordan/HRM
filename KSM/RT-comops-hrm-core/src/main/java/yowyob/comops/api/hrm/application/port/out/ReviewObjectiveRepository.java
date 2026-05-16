package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.ReviewObjective;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewObjectiveRepository {

    Mono<ReviewObjective> save(ReviewObjective objective);

    Mono<ReviewObjective> findById(UUID tenantId, UUID objectiveId);

    Flux<ReviewObjective> findByReviewId(UUID tenantId, UUID reviewId);
}
