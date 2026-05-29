package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.ExpenseLine;
import yowyob.comops.api.hrm.domain.model.ExpenseReport;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageExpenseUseCase {

    Mono<ExpenseReport> createExpenseReport(CreateExpenseReportCommand command);

    Mono<ExpenseLine> addExpenseLine(AddExpenseLineCommand command);

    Mono<ExpenseReport> submitExpenseReport(UUID expenseReportId);

    Mono<ExpenseReport> approveExpenseReport(UUID expenseReportId);

    Mono<ExpenseReport> rejectExpenseReport(UUID expenseReportId);

    Mono<ExpenseReport> reimburseExpenseReport(UUID expenseReportId);

    Mono<ExpenseReport> getExpenseReport(UUID expenseReportId);

    Flux<ExpenseReport> listExpenseReportsByEmployee(UUID employeeId);

    /**
     * Org-wide list of expense reports in the tenant. When {@code status} is null
     * all statuses are returned, otherwise only the matching ones. Used by the
     * accountant / DAF validation queue.
     */
    Flux<ExpenseReport> listExpenseReports(UUID organizationId, String status);

    Flux<ExpenseLine> listExpenseLinesByReport(UUID expenseReportId);
}
