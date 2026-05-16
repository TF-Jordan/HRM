package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RhKpiSnapshotSpringDataRepository extends ReactiveCrudRepository<RhKpiSnapshotEntity, UUID> {

    Mono<RhKpiSnapshotEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<RhKpiSnapshotEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
