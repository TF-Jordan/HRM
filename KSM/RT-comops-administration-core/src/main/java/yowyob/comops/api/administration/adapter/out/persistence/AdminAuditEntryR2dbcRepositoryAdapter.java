package yowyob.comops.api.administration.adapter.out.persistence;

import yowyob.comops.api.administration.application.port.out.AdminAuditRepository;
import yowyob.comops.api.administration.domain.model.AdminAuditEntry;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class AdminAuditEntryR2dbcRepositoryAdapter implements AdminAuditRepository {

    private final AdminAuditEntrySpringDataRepository repository;

    public AdminAuditEntryR2dbcRepositoryAdapter(AdminAuditEntrySpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<AdminAuditEntry> save(AdminAuditEntry entry) {
        return repository.save(new AdminAuditEntryEntity(entry.id(), entry.tenantId(), entry.createdAt(),
                entry.updatedAt(), entry.organizationId(), entry.actorUserId(), entry.action(), entry.targetType(),
                entry.targetId(), entry.payloadSummary())).map(this::toDomain);
    }

    @Override
    public Flux<AdminAuditEntry> findByTenantId(UUID tenantId, int limit) {
        return repository.findTop200ByTenantIdOrderByCreatedAtDesc(tenantId)
                .take(limit)
                .map(this::toDomain);
    }

    private AdminAuditEntry toDomain(AdminAuditEntryEntity entity) {
        return AdminAuditEntry.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.actorUserId(), entity.action(), entity.targetType(), entity.targetId(),
                entity.payloadSummary());
    }
}
