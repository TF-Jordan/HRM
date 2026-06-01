package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewObjectiveSpringDataRepository extends ReactiveCrudRepository<ReviewObjectiveEntity, UUID> {

    Mono<ReviewObjectiveEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<ReviewObjectiveEntity> findAllByTenantIdAndReviewId(UUID tenantId, UUID reviewId);
}
