package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.resource.application.port.out.ResourceAssignmentRepository;
import yowyob.comops.api.resource.domain.model.ResourceAssignment;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ResourceAssignmentR2dbcRepositoryAdapter implements ResourceAssignmentRepository {
    private final ResourceAssignmentSpringDataRepository repository;

    public ResourceAssignmentR2dbcRepositoryAdapter(ResourceAssignmentSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<ResourceAssignment> save(ResourceAssignment assignment) {
        ResourceAssignmentEntity entity = new ResourceAssignmentEntity(assignment.id(), assignment.tenantId(),
                assignment.createdAt(), assignment.updatedAt(), assignment.resourceId(), assignment.assigneeType(),
                assignment.assigneeId(), assignment.assignedAt(), assignment.status(), assignment.unassignedAt());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<ResourceAssignment> findActiveByTenantIdAndResourceId(UUID tenantId, UUID resourceId) {
        return repository.findFirstByTenantIdAndResourceIdAndStatusOrderByAssignedAtDesc(tenantId, resourceId, "ACTIVE")
                .map(this::toDomain);
    }

    @Override
    public Flux<ResourceAssignment> findByTenantIdAndResourceId(UUID tenantId, UUID resourceId) {
        return repository.findAllByTenantIdAndResourceIdOrderByAssignedAtDesc(tenantId, resourceId)
                .map(this::toDomain);
    }

    @Override
    public Flux<ResourceAssignment> findByTenantIdAndAssigneeTypeAndAssigneeId(UUID tenantId, String assigneeType,
            UUID assigneeId) {
        return repository.findAllByTenantIdAndAssigneeTypeAndAssigneeIdOrderByAssignedAtDesc(
                        tenantId, assigneeType.toUpperCase(), assigneeId)
                .map(this::toDomain);
    }

    private ResourceAssignment toDomain(ResourceAssignmentEntity entity) {
        return ResourceAssignment.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.resourceId(), entity.assigneeType(), entity.assigneeId(), entity.assignedAt(), entity.status(),
                entity.unassignedAt());
    }
}
