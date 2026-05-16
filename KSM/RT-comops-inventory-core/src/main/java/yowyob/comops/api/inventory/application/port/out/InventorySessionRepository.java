package yowyob.comops.api.inventory.application.port.out;

import yowyob.comops.api.inventory.domain.model.InventorySession;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InventorySessionRepository {

    Mono<InventorySession> save(InventorySession inventorySession);

    Mono<InventorySession> findById(UUID tenantId, UUID inventorySessionId);

    Flux<InventorySession> findByOrganization(UUID tenantId, UUID organizationId);
}
