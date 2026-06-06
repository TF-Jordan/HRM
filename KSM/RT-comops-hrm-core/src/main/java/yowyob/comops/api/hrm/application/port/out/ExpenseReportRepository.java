package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.ExpenseReport;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpenseReportRepository {

    Mono<ExpenseReport> save(ExpenseReport report);

    Mono<ExpenseReport> findById(UUID tenantId, UUID reportId);

    Flux<ExpenseReport> findByEmployeeId(UUID tenantId, UUID employeeId);

    /** Every expense report of the tenant, regardless of status. */
    Flux<ExpenseReport> findAll(UUID tenantId);

    /** Every expense report of the tenant whose status matches the given value. */
    Flux<ExpenseReport> findByStatus(UUID tenantId, String status);

    /** Every expense report attached to the given mission order (advance regularization). */
    Flux<ExpenseReport> findByMissionOrderId(UUID tenantId, UUID missionOrderId);
}
