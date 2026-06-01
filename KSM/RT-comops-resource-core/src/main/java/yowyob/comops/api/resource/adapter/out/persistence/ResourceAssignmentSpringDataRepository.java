package yowyob.comops.api.resource.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ResourceAssignmentSpringDataRepository extends ReactiveCrudRepository<ResourceAssignmentEntity, UUID> {
    reactor.core.publisher.Mono<ResourceAssignmentEntity> findFirstByTenantIdAndResourceIdAndStatusOrderByAssignedAtDesc(
            UUID tenantId, UUID resourceId, String status);
    Flux<ResourceAssignmentEntity> findAllByTenantIdAndResourceIdOrderByAssignedAtDesc(UUID tenantId, UUID resourceId);
    Flux<ResourceAssignmentEntity> findAllByTenantIdAndAssigneeTypeAndAssigneeIdOrderByAssignedAtDesc(
            UUID tenantId, String assigneeType, UUID assigneeId);
}
