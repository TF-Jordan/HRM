package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApplicationSpringDataRepository extends ReactiveCrudRepository<ApplicationEntity, UUID> {

    Mono<ApplicationEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<ApplicationEntity> findAllByTenantIdAndJobOfferId(UUID tenantId, UUID jobOfferId);
}
