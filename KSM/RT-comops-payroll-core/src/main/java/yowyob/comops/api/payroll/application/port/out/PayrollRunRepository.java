package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.PayrollRun;

import java.util.UUID;

/**
 * Persistence port for {@link PayrollRun}. The {@code period} parameter is the canonical
 * {@code YYYY-MM} string ({@code PayPeriod.format()}), matching the stored column.
 */
public interface PayrollRunRepository {

    Mono<PayrollRun> save(PayrollRun run);

    Mono<PayrollRun> findById(UUID tenantId, UUID id);

    /** The run for an organization-wide period (agency-less). Used for uniqueness checks. */
    Mono<PayrollRun> findByOrganizationAndPeriodAndType(UUID tenantId, UUID organizationId,
                                                        String period, String runType);

    /** The run for an agency-scoped period. Used for uniqueness checks. */
    Mono<PayrollRun> findByOrganizationAndAgencyAndPeriodAndType(UUID tenantId, UUID organizationId,
                                                                 UUID agencyId, String period, String runType);

    Flux<PayrollRun> findByOrganization(UUID tenantId, UUID organizationId);
}
