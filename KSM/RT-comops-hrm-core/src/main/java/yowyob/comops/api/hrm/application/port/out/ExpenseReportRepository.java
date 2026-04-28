package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.ExpenseReport;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpenseReportRepository {

    Mono<ExpenseReport> save(ExpenseReport report);

    Mono<ExpenseReport> findById(UUID tenantId, UUID reportId);

    Flux<ExpenseReport> findByEmployeeId(UUID tenantId, UUID employeeId);
}
