package yowyob.comops.api.payroll.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GarnishmentOrderSpringDataRepository
        extends ReactiveCrudRepository<GarnishmentOrderEntity, UUID> {

    Mono<GarnishmentOrderEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<GarnishmentOrderEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<GarnishmentOrderEntity> findAllByTenantIdAndEmployeeIdAndStatus(
            UUID tenantId, UUID employeeId, String status);

    Flux<GarnishmentOrderEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
