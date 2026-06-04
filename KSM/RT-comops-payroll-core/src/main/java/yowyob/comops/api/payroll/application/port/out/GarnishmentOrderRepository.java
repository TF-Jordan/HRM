package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.GarnishmentOrder;

import java.util.UUID;

/** Persistence port for {@link GarnishmentOrder}. */
public interface GarnishmentOrderRepository {

    Mono<GarnishmentOrder> save(GarnishmentOrder order);

    Mono<GarnishmentOrder> findById(UUID tenantId, UUID id);

    Flux<GarnishmentOrder> findByEmployee(UUID tenantId, UUID employeeId);

    /** Active orders for an employee, used by the run pipeline. */
    Flux<GarnishmentOrder> findActiveByEmployee(UUID tenantId, UUID employeeId);

    Flux<GarnishmentOrder> findByOrganization(UUID tenantId, UUID organizationId);
}
