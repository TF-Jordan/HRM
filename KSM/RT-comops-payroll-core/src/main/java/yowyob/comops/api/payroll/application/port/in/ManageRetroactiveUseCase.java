package yowyob.comops.api.payroll.application.port.in;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.RetroactiveAdjustment;

import java.util.UUID;

/** Computation and lifecycle of retroactive pay adjustments. */
public interface ManageRetroactiveUseCase {

    Mono<RetroactiveAdjustment> calculate(CalculateRetroactiveCommand command);

    Mono<RetroactiveAdjustment> apply(UUID adjustmentId);

    Mono<RetroactiveAdjustment> cancel(UUID adjustmentId);

    Mono<RetroactiveAdjustment> get(UUID adjustmentId);

    Flux<RetroactiveAdjustment> listForEmployee(UUID employeeId);

    Flux<RetroactiveAdjustment> listForOrganization(UUID organizationId);
}
