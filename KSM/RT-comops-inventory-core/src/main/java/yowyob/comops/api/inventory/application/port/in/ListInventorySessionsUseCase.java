package yowyob.comops.api.inventory.application.port.in;

import yowyob.comops.api.inventory.domain.model.InventorySession;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListInventorySessionsUseCase {

    Flux<InventorySession> listSessions(UUID organizationId);
}
