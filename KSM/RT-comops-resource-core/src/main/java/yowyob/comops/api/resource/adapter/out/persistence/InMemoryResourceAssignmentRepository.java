package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.resource.application.port.out.ResourceAssignmentRepository;
import yowyob.comops.api.resource.domain.model.ResourceAssignment;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryResourceAssignmentRepository implements ResourceAssignmentRepository {
    private final Map<UUID, ResourceAssignment> store = new ConcurrentHashMap<>();

    @Override
    public Mono<ResourceAssignment> save(ResourceAssignment assignment) {
        return Mono.fromSupplier(() -> { store.put(assignment.id(), assignment); return assignment; });
    }

    @Override
    public Mono<ResourceAssignment> findActiveByTenantIdAndResourceId(UUID tenantId, UUID resourceId) {
        return Flux.fromStream(store.values().stream()
                        .filter(item -> item.tenantId().equals(tenantId))
                        .filter(item -> item.resourceId().equals(resourceId))
                        .filter(item -> "ACTIVE".equals(item.status()))
                        .sorted((a, b) -> b.assignedAt().compareTo(a.assignedAt())))
                .next();
    }

    @Override
    public Flux<ResourceAssignment> findByTenantIdAndResourceId(UUID tenantId, UUID resourceId) {
        return Flux.fromStream(store.values().stream()
                .filter(item -> item.tenantId().equals(tenantId))
                .filter(item -> item.resourceId().equals(resourceId))
                .sorted((a, b) -> b.assignedAt().compareTo(a.assignedAt())));
    }

    @Override
    public Flux<ResourceAssignment> findByTenantIdAndAssigneeTypeAndAssigneeId(UUID tenantId, String assigneeType,
            UUID assigneeId) {
        String normalizedType = assigneeType == null ? null : assigneeType.trim().toUpperCase();
        return Flux.fromStream(store.values().stream()
                .filter(item -> item.tenantId().equals(tenantId))
                .filter(item -> item.assigneeId().equals(assigneeId))
                .filter(item -> item.assigneeType().equals(normalizedType))
                .sorted((a, b) -> b.assignedAt().compareTo(a.assignedAt())));
    }
}
