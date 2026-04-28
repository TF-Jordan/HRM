package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.ExpenseLine;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpenseLineRepository {

    Mono<ExpenseLine> save(ExpenseLine line);

    Flux<ExpenseLine> findByExpenseReportId(UUID tenantId, UUID expenseReportId);
}
