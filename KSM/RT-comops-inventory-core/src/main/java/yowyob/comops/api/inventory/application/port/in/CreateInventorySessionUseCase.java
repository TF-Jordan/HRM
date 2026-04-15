package yowyob.comops.api.inventory.application.port.in;

import yowyob.comops.api.inventory.domain.model.InventorySession;
import reactor.core.publisher.Mono;

public interface CreateInventorySessionUseCase {

    Mono<InventorySession> createSession(CreateInventorySessionCommand command);
}
