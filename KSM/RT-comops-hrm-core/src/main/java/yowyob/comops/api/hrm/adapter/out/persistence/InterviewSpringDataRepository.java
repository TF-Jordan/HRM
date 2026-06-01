package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InterviewSpringDataRepository extends ReactiveCrudRepository<InterviewEntity, UUID> {

    Mono<InterviewEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<InterviewEntity> findAllByTenantIdAndApplicationId(UUID tenantId, UUID applicationId);
}
