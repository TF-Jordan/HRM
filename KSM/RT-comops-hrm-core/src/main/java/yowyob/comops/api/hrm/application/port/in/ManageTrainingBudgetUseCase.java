package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.TrainingBudget;

import java.math.BigDecimal;
import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageTrainingBudgetUseCase {

    Mono<TrainingBudget> createBudget(CreateTrainingBudgetCommand command);

    Mono<TrainingBudget> getBudget(UUID budgetId);

    Flux<TrainingBudget> listBudgets(UUID organizationId, int annee);

    Mono<TrainingBudget> engageBudget(UUID budgetId, BigDecimal montant);

    Mono<TrainingBudget> realiseBudget(UUID budgetId, BigDecimal montant);
}
