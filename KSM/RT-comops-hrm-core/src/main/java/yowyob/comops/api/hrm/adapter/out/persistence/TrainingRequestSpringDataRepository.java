package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TrainingRequestSpringDataRepository extends ReactiveCrudRepository<TrainingRequestEntity, UUID> {

    Mono<TrainingRequestEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<TrainingRequestEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<TrainingRequestEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);

    Flux<TrainingRequestEntity> findAllByTenantIdAndOrganizationIdAndStatus(
            UUID tenantId, UUID organizationId, String status);
}
