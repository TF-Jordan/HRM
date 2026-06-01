package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.TrainingBudget;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TrainingBudgetRepository {

    Mono<TrainingBudget> save(TrainingBudget budget);

    Mono<TrainingBudget> findById(UUID tenantId, UUID budgetId);

    Flux<TrainingBudget> findByOrganizationIdAndAnnee(UUID tenantId, UUID organizationId, int annee);
}
