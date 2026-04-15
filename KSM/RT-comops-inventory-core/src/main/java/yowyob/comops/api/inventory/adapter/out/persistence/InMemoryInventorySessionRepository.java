package yowyob.comops.api.inventory.adapter.out.persistence;

import yowyob.comops.api.inventory.application.port.out.InventorySessionRepository;
import yowyob.comops.api.inventory.domain.model.InventorySession;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryInventorySessionRepository implements InventorySessionRepository {

    private final Map<UUID, InventorySession> sessions = new ConcurrentHashMap<>();

    @Override
    public Mono<InventorySession> save(InventorySession inventorySession) {
        return Mono.fromSupplier(() -> {
            sessions.put(inventorySession.id(), inventorySession);
            return inventorySession;
        });
    }

    @Override
    public Mono<InventorySession> findById(UUID tenantId, UUID inventorySessionId) {
        return Mono.justOrEmpty(sessions.get(inventorySessionId))
                .filter(session -> session.tenantId().equals(tenantId));
    }

    @Override
    public Flux<InventorySession> findByOrganization(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(sessions.values().stream()
                .filter(session -> session.tenantId().equals(tenantId))
                .filter(session -> session.organizationId().equals(organizationId)));
    }
}
