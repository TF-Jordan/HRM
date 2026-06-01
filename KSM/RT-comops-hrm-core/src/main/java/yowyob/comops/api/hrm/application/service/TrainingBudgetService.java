package yowyob.comops.api.hrm.application.service;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.hrm.application.port.in.CreateTrainingBudgetCommand;
import yowyob.comops.api.hrm.application.port.in.ManageTrainingBudgetUseCase;
import yowyob.comops.api.hrm.application.port.out.TrainingBudgetRepository;
import yowyob.comops.api.hrm.domain.model.TrainingBudget;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@Service
public class TrainingBudgetService implements ManageTrainingBudgetUseCase {

    private final TrainingBudgetRepository trainingBudgetRepository;

    public TrainingBudgetService(TrainingBudgetRepository trainingBudgetRepository) {
        this.trainingBudgetRepository = trainingBudgetRepository;
    }

    @Override
    public Mono<TrainingBudget> createBudget(CreateTrainingBudgetCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> {
                    TrainingBudget budget = TrainingBudget.create(ctx.tenantId(), command.organizationId(),
                            command.agencyId(), command.annee(), command.montantAlloue());
                    return trainingBudgetRepository.save(budget);
                });
    }

    @Override
    public Mono<TrainingBudget> getBudget(UUID budgetId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> trainingBudgetRepository.findById(ctx.tenantId(), budgetId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Training budget not found"))));
    }

    @Override
    public Flux<TrainingBudget> listBudgets(UUID organizationId, int annee) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> trainingBudgetRepository.findByOrganizationIdAndAnnee(
                        ctx.tenantId(), organizationId, annee));
    }

    @Override
    public Mono<TrainingBudget> engageBudget(UUID budgetId, BigDecimal montant) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> trainingBudgetRepository.findById(ctx.tenantId(), budgetId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Training budget not found")))
                        .map(budget -> budget.engage(montant))
                        .flatMap(trainingBudgetRepository::save));
    }

    @Override
    public Mono<TrainingBudget> realiseBudget(UUID budgetId, BigDecimal montant) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> trainingBudgetRepository.findById(ctx.tenantId(), budgetId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Training budget not found")))
                        .map(budget -> budget.realiser(montant))
                        .flatMap(trainingBudgetRepository::save));
    }
}
