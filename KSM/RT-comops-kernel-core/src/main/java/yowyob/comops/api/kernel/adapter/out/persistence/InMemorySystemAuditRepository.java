package yowyob.comops.api.kernel.adapter.out.persistence;

import yowyob.comops.api.kernel.application.port.out.SystemAuditRepository;
import yowyob.comops.api.kernel.domain.model.SystemAuditEntry;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemorySystemAuditRepository implements SystemAuditRepository {

    private final Map<UUID, SystemAuditEntry> entries = new ConcurrentHashMap<>();

    @Override
    public Mono<SystemAuditEntry> save(SystemAuditEntry entry) {
        return Mono.fromSupplier(() -> {
            entries.put(entry.id(), entry);
            return entry;
        });
    }

    @Override
    public Flux<SystemAuditEntry> findByTenantIdAndActorUserId(UUID tenantId, UUID actorUserId, int limit) {
        return Flux.fromStream(entries.values().stream()
                .filter(entry -> entry.tenantId().equals(tenantId))
                .filter(entry -> actorUserId != null && actorUserId.equals(entry.actorUserId()))
                .sorted(Comparator.comparing(SystemAuditEntry::createdAt).reversed())
                .limit(limit));
    }

    @Override
    public Flux<SystemAuditEntry> findByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId, int limit) {
        return Flux.fromStream(entries.values().stream()
                .filter(entry -> entry.tenantId().equals(tenantId))
                .filter(entry -> organizationId != null && organizationId.equals(entry.organizationId()))
                .sorted(Comparator.comparing(SystemAuditEntry::createdAt).reversed())
                .limit(limit));
    }
}
