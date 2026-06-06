package yowyob.comops.api.hrm.application.port.in;

import java.util.UUID;

import reactor.core.publisher.Mono;

/**
 * Drives the monthly leave accrual: credits each active employee's ANNUAL leave balance with the
 * month's earned days (base rate + seniority + dependent-children bonuses). The operation is
 * idempotent per calendar month — a balance already accrued for the current month is skipped — so
 * it is safe to invoke repeatedly (scheduled cron, manual trigger, restart catch-up).
 */
public interface RunLeaveAccrualUseCase {

    /** Runs the accrual for one organization. Returns the number of balances credited. */
    Mono<Integer> runMonthlyAccrual(UUID tenantId, UUID organizationId);

    /** Runs the accrual for every organization that has at least one active employee. */
    Mono<Integer> runForAllActiveOrganizations();

    /** Runs the accrual for the caller's current tenant/organization context. */
    Mono<Integer> runForCurrentContext();
}
