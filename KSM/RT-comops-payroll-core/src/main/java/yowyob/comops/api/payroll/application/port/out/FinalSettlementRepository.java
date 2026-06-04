package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.FinalSettlement;

import java.util.UUID;

/** Persistence port for {@link FinalSettlement}. */
public interface FinalSettlementRepository {

    Mono<FinalSettlement> save(FinalSettlement settlement);

    Mono<FinalSettlement> findById(UUID tenantId, UUID id);

    Flux<FinalSettlement> findByEmployee(UUID tenantId, UUID employeeId);

    Flux<FinalSettlement> findByOrganization(UUID tenantId, UUID organizationId);
}
