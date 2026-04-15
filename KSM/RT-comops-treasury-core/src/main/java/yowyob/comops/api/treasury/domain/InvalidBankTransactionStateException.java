package yowyob.comops.api.treasury.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class InvalidBankTransactionStateException extends DomainException {

    public InvalidBankTransactionStateException(UUID transactionId, String currentStatus, String expectedStatus) {
        super("bank transaction " + transactionId + " is in status '" + currentStatus
                + "' but expected '" + expectedStatus + "'");
    }
}
