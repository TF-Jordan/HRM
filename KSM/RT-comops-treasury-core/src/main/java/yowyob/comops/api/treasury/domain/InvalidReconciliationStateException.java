package yowyob.comops.api.treasury.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public class InvalidReconciliationStateException extends DomainException {

    public InvalidReconciliationStateException(UUID reconciliationId, String currentStatus, String expectedStatus) {
        super("Reconciliation " + reconciliationId + " is in status " + currentStatus + " but expected "
                + expectedStatus + ".");
    }
}
