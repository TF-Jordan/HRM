package yowyob.comops.api.treasury.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class InvalidCheckPaymentStateException extends DomainException {
    public InvalidCheckPaymentStateException(UUID checkPaymentId, String currentStatus, String expectedStatus) {
        super("Check payment " + checkPaymentId + " is in status " + currentStatus + " but expected "
                + expectedStatus + '.');
    }
}
