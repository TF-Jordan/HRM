package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.RetroactiveAdjustment;

import java.util.UUID;

/** Persistence port for {@link RetroactiveAdjustment}. */
public interface RetroactiveAdjustmentRepository {

    Mono<RetroactiveAdjustment> save(RetroactiveAdjustment adjustment);

    Mono<RetroactiveAdjustment> findById(UUID tenantId, UUID id);

    Flux<RetroactiveAdjustment> findByEmployee(UUID tenantId, UUID employeeId);

    Flux<RetroactiveAdjustment> findByOrganization(UUID tenantId, UUID organizationId);
}
