package yowyob.comops.api.administration.adapter.out.persistence;

import yowyob.comops.api.administration.application.port.out.AdminAuditRepository;
import yowyob.comops.api.administration.domain.model.AdminAuditEntry;
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
public class InMemoryAdminAuditRepository implements AdminAuditRepository {

    private final Map<UUID, AdminAuditEntry> entries = new ConcurrentHashMap<>();

    @Override
    public Mono<AdminAuditEntry> save(AdminAuditEntry entry) {
        return Mono.fromSupplier(() -> {
            entries.put(entry.id(), entry);
            return entry;
        });
    }

    @Override
    public Flux<AdminAuditEntry> findByTenantId(UUID tenantId, int limit) {
        return Flux.fromStream(entries.values().stream()
                .filter(entry -> entry.tenantId().equals(tenantId))
                .sorted(Comparator.comparing(AdminAuditEntry::createdAt).reversed())
                .limit(limit));
    }
}
