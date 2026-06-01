package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TrainingBudgetSpringDataRepository extends ReactiveCrudRepository<TrainingBudgetEntity, UUID> {

    Mono<TrainingBudgetEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<TrainingBudgetEntity> findAllByTenantIdAndOrganizationIdAndAnnee(UUID tenantId, UUID organizationId, int annee);
}
