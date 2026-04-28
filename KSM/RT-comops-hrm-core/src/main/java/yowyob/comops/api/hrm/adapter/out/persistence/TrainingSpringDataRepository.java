package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TrainingSpringDataRepository extends ReactiveCrudRepository<TrainingEntity, UUID> {

    Mono<TrainingEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<TrainingEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
