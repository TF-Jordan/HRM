package yowyob.comops.api.inventory.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public class InvalidWarehouseTransferStateException extends DomainException {

    public InvalidWarehouseTransferStateException(UUID transferId, String currentStatus, String expectedStatus) {
        super("Warehouse transfer " + transferId + " is in status " + currentStatus + " but expected "
                + expectedStatus + ".");
    }
}
