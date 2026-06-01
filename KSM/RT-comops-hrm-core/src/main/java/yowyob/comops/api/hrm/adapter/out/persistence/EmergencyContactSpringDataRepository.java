package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmergencyContactSpringDataRepository
        extends ReactiveCrudRepository<EmergencyContactEntity, UUID> {

    Flux<EmergencyContactEntity> findAllByTenantIdAndEmployeeIdOrderByPrioriteAsc(UUID tenantId, UUID employeeId);

    Mono<EmergencyContactEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Mono<Void> deleteByIdAndTenantId(UUID id, UUID tenantId);
}
