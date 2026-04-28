package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.ExpenseLineRepository;
import yowyob.comops.api.hrm.domain.model.ExpenseLine;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ExpenseLineR2dbcRepositoryAdapter implements ExpenseLineRepository {

    private final ExpenseLineSpringDataRepository repository;

    public ExpenseLineR2dbcRepositoryAdapter(ExpenseLineSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<ExpenseLine> save(ExpenseLine line) {
        return repository.save(toEntity(line)).map(this::toDomain);
    }

    @Override
    public Flux<ExpenseLine> findByExpenseReportId(UUID tenantId, UUID expenseReportId) {
        return repository.findAllByTenantIdAndExpenseReportId(tenantId, expenseReportId).map(this::toDomain);
    }

    private ExpenseLineEntity toEntity(ExpenseLine l) {
        return new ExpenseLineEntity(l.id(), l.tenantId(), l.expenseReportId(), l.description(),
                l.montant(), l.categorie(), l.justificatifFileId());
    }

    private ExpenseLine toDomain(ExpenseLineEntity e) {
        return new ExpenseLine(e.id(), e.tenantId(), e.expenseReportId(), e.description(),
                e.montant(), e.categorie(), e.justificatifFileId());
    }
}
