package yowyob.comops.api.resource.application.port.out;

import yowyob.comops.api.resource.domain.model.ResourceAssignment;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ResourceAssignmentRepository {
    Mono<ResourceAssignment> save(ResourceAssignment assignment);
    Mono<ResourceAssignment> findActiveByTenantIdAndResourceId(UUID tenantId, UUID resourceId);
    Flux<ResourceAssignment> findByTenantIdAndResourceId(UUID tenantId, UUID resourceId);
    Flux<ResourceAssignment> findByTenantIdAndAssigneeTypeAndAssigneeId(UUID tenantId, String assigneeType, UUID assigneeId);
}
