package yowyob.comops.api.inventory.application.port.in;

import yowyob.comops.api.inventory.domain.model.InventorySession;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ValidateInventorySessionUseCase {

    Mono<InventorySession> validateSession(UUID inventorySessionId);
}
