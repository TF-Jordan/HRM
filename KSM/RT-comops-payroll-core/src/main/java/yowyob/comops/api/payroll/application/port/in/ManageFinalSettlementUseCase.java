package yowyob.comops.api.payroll.application.port.in;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.FinalSettlement;

import java.util.UUID;

/** Computation and consultation of final settlements (soldes de tout compte). */
public interface ManageFinalSettlementUseCase {

    Mono<FinalSettlement> calculate(CalculateFinalSettlementCommand command);

    Mono<FinalSettlement> markPaid(UUID settlementId);

    Mono<FinalSettlement> get(UUID settlementId);

    Flux<FinalSettlement> listForEmployee(UUID employeeId);

    Flux<FinalSettlement> listForOrganization(UUID organizationId);
}
