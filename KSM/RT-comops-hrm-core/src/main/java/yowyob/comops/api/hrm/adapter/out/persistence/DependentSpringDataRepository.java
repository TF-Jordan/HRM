package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DependentSpringDataRepository extends ReactiveCrudRepository<DependentEntity, UUID> {

    Flux<DependentEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Mono<Void> deleteByIdAndTenantId(UUID id, UUID tenantId);
}
