package yowyob.comops.api.treasury.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class CheckPaymentNotFoundException extends DomainException {
    public CheckPaymentNotFoundException(UUID checkPaymentId) {
        super("Check payment not found: " + checkPaymentId);
    }
}
