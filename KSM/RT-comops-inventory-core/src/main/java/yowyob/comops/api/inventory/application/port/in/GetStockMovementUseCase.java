package yowyob.comops.api.inventory.application.port.in;

import yowyob.comops.api.inventory.domain.model.StockMovement;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface GetStockMovementUseCase {

    Mono<StockMovement> getMovement(UUID movementId);
}
