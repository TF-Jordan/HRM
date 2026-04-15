package yowyob.comops.api.inventory.application.port.in;

import yowyob.comops.api.inventory.domain.model.StockMovement;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListStockMovementsUseCase {

    Flux<StockMovement> listMovements(UUID organizationId, UUID agencyId, UUID productId);
}
