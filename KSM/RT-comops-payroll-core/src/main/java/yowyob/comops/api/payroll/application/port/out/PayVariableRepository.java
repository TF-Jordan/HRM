package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.PayVariable;

import java.util.UUID;

/**
 * Persistence port for {@link PayVariable}. The {@code period} parameter is the canonical
 * {@code YYYY-MM} string.
 */
public interface PayVariableRepository {

    Mono<PayVariable> save(PayVariable variable);

    Mono<PayVariable> findByEmployeeAndPeriod(UUID tenantId, UUID employeeId, String period);

    Flux<PayVariable> findByOrganizationAndPeriod(UUID tenantId, UUID organizationId, String period);
}
