package yowyob.comops.api.kernel.adapter.out.persistence;

import yowyob.comops.api.kernel.application.port.out.SystemAuditRepository;
import yowyob.comops.api.kernel.domain.model.SystemAuditEntry;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class SystemAuditR2dbcRepositoryAdapter implements SystemAuditRepository {

    private final SystemAuditEntrySpringDataRepository repository;

    public SystemAuditR2dbcRepositoryAdapter(SystemAuditEntrySpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<SystemAuditEntry> save(SystemAuditEntry entry) {
        return repository.save(toEntity(entry)).map(this::toDomain);
    }

    @Override
    public Flux<SystemAuditEntry> findByTenantIdAndActorUserId(UUID tenantId, UUID actorUserId, int limit) {
        return repository.findTop200ByTenantIdAndActorUserIdOrderByCreatedAtDesc(tenantId, actorUserId)
                .take(limit)
                .map(this::toDomain);
    }

    @Override
    public Flux<SystemAuditEntry> findByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId, int limit) {
        return repository.findTop200ByTenantIdAndOrganizationIdOrderByCreatedAtDesc(tenantId, organizationId)
                .take(limit)
                .map(this::toDomain);
    }

    private SystemAuditEntryEntity toEntity(SystemAuditEntry entry) {
        return new SystemAuditEntryEntity(entry.id(), entry.tenantId(), entry.createdAt(), entry.updatedAt(),
                entry.organizationId(), entry.actorUserId(), entry.action(), entry.targetType(), entry.targetId(),
                entry.payloadSummary());
    }

    private SystemAuditEntry toDomain(SystemAuditEntryEntity entity) {
        return SystemAuditEntry.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.actorUserId(), entity.action(), entity.targetType(), entity.targetId(),
                entity.payloadSummary());
    }
}
