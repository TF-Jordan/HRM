package yowyob.comops.api.inventory.application.port.in;

import yowyob.comops.api.inventory.domain.model.StockMovement;
import reactor.core.publisher.Mono;

public interface RecordStockMovementUseCase {

    Mono<StockMovement> recordMovement(RecordStockMovementCommand command);
}
