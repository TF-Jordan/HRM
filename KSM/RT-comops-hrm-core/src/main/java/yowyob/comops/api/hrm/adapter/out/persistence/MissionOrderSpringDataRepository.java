package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MissionOrderSpringDataRepository extends ReactiveCrudRepository<MissionOrderEntity, UUID> {

    Mono<MissionOrderEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<MissionOrderEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);
}
