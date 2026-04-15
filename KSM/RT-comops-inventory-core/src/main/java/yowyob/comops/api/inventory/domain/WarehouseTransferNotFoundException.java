package yowyob.comops.api.inventory.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public class WarehouseTransferNotFoundException extends DomainException {

    public WarehouseTransferNotFoundException(UUID transferId) {
        super("Warehouse transfer " + transferId + " was not found.");
    }
}
