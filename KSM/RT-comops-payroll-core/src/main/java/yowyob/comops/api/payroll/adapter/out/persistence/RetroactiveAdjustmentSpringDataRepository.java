package yowyob.comops.api.payroll.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RetroactiveAdjustmentSpringDataRepository
        extends ReactiveCrudRepository<RetroactiveAdjustmentEntity, UUID> {

    Mono<RetroactiveAdjustmentEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<RetroactiveAdjustmentEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<RetroactiveAdjustmentEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
