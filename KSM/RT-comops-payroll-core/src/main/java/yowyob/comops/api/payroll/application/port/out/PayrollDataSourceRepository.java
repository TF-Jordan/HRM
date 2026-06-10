package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Per-organization choice of where payroll reads its employee data:
 *
 * <ul>
 *   <li>{@code HRM} (default) — employees, loans, leave and timesheets come from hrm-core
 *       via the bootstrap bridging adapter;</li>
 *   <li>{@code LOCAL} — employees come from the payroll-owned {@code payroll_employee} table
 *       (CSV import / CRUD); loans, leave and timesheets are not sourced (variables are
 *       captured manually through pay variables).</li>
 * </ul>
 *
 * The first CSV import flips an organization to {@code LOCAL}; it can be switched back
 * explicitly through the data-source endpoint.
 */
public interface PayrollDataSourceRepository {

    enum Source { HRM, LOCAL }

    /** The configured source, defaulting to {@code HRM} when no row exists. */
    Mono<Source> get(UUID tenantId, UUID organizationId);

    Mono<Void> set(UUID tenantId, UUID organizationId, Source source);
}
