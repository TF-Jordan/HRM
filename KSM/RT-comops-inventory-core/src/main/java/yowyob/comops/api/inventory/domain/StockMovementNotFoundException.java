package yowyob.comops.api.inventory.domain;

import java.util.UUID;

public class StockMovementNotFoundException extends RuntimeException {

    public StockMovementNotFoundException(UUID movementId) {
        super("Stock movement not found: " + movementId);
    }
}
