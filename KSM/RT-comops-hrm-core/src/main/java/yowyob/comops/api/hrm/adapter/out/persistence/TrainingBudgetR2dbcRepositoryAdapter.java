package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.TrainingBudgetRepository;
import yowyob.comops.api.hrm.domain.model.TrainingBudget;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class TrainingBudgetR2dbcRepositoryAdapter implements TrainingBudgetRepository {

    private final TrainingBudgetSpringDataRepository repository;

    public TrainingBudgetR2dbcRepositoryAdapter(TrainingBudgetSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<TrainingBudget> save(TrainingBudget budget) {
        return repository.save(toEntity(budget)).map(this::toDomain);
    }

    @Override
    public Mono<TrainingBudget> findById(UUID tenantId, UUID budgetId) {
        return repository.findByIdAndTenantId(budgetId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<TrainingBudget> findByOrganizationIdAndAnnee(UUID tenantId, UUID organizationId, int annee) {
        return repository.findAllByTenantIdAndOrganizationIdAndAnnee(tenantId, organizationId, annee)
                .map(this::toDomain);
    }

    private TrainingBudgetEntity toEntity(TrainingBudget b) {
        return new TrainingBudgetEntity(b.id(), b.tenantId(), b.createdAt(), b.updatedAt(),
                b.organizationId(), b.agencyId(), b.annee(),
                b.montantAlloue(), b.montantEngage(), b.montantRealise());
    }

    private TrainingBudget toDomain(TrainingBudgetEntity e) {
        return TrainingBudget.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.agencyId(), e.annee(),
                e.montantAlloue(), e.montantEngage(), e.montantRealise());
    }
}
