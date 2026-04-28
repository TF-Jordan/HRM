package yowyob.comops.api.hrm.application.service;

import yowyob.comops.api.hrm.application.port.in.*;
import yowyob.comops.api.hrm.application.port.out.ExpenseLineRepository;
import yowyob.comops.api.hrm.application.port.out.ExpenseReportRepository;
import yowyob.comops.api.hrm.domain.model.ExpenseLine;
import yowyob.comops.api.hrm.domain.model.ExpenseReport;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ExpenseService implements ManageExpenseUseCase {

    private final ExpenseReportRepository expenseReportRepository;
    private final ExpenseLineRepository expenseLineRepository;

    public ExpenseService(ExpenseReportRepository expenseReportRepository,
                          ExpenseLineRepository expenseLineRepository) {
        this.expenseReportRepository = expenseReportRepository;
        this.expenseLineRepository = expenseLineRepository;
    }

    @Override
    public Mono<ExpenseReport> createExpenseReport(CreateExpenseReportCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> {
                    ExpenseReport report = ExpenseReport.create(ctx.tenantId(), command.employeeId(),
                            command.periode(), command.motif());
                    return expenseReportRepository.save(report);
                });
    }

    @Override
    public Mono<ExpenseLine> addExpenseLine(AddExpenseLineCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> expenseReportRepository.findById(ctx.tenantId(), command.expenseReportId())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Expense report not found")))
                        .flatMap(report -> {
                            ExpenseLine line = ExpenseLine.create(ctx.tenantId(), command.expenseReportId(),
                                    command.description(), command.montant(), command.categorie(),
                                    command.justificatifFileId());
                            return expenseLineRepository.save(line)
                                    .flatMap(saved -> expenseLineRepository.findByExpenseReportId(ctx.tenantId(), report.id())
                                            .map(ExpenseLine::montant)
                                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                                            .flatMap(total -> expenseReportRepository.save(report.withTotalMontant(total)))
                                            .thenReturn(saved));
                        }));
    }

    @Override
    public Mono<ExpenseReport> submitExpenseReport(UUID expenseReportId) {
        return updateExpenseReport(expenseReportId, ExpenseReport::submit);
    }

    @Override
    public Mono<ExpenseReport> approveExpenseReport(UUID expenseReportId) {
        return updateExpenseReport(expenseReportId, ExpenseReport::approve);
    }

    @Override
    public Mono<ExpenseReport> rejectExpenseReport(UUID expenseReportId) {
        return updateExpenseReport(expenseReportId, ExpenseReport::reject);
    }

    @Override
    public Mono<ExpenseReport> reimburseExpenseReport(UUID expenseReportId) {
        return updateExpenseReport(expenseReportId, ExpenseReport::reimburse);
    }

    @Override
    public Mono<ExpenseReport> getExpenseReport(UUID expenseReportId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> expenseReportRepository.findById(ctx.tenantId(), expenseReportId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Expense report not found"))));
    }

    @Override
    public Flux<ExpenseReport> listExpenseReportsByEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> expenseReportRepository.findByEmployeeId(ctx.tenantId(), employeeId));
    }

    @Override
    public Flux<ExpenseLine> listExpenseLinesByReport(UUID expenseReportId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> expenseLineRepository.findByExpenseReportId(ctx.tenantId(), expenseReportId));
    }

    private Mono<ExpenseReport> updateExpenseReport(UUID id, java.util.function.Function<ExpenseReport, ExpenseReport> transition) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> expenseReportRepository.findById(ctx.tenantId(), id)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Expense report not found")))
                        .map(transition)
                        .flatMap(expenseReportRepository::save));
    }
}
