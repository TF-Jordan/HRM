package yowyob.comops.api.treasury.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class BankTransactionNotFoundException extends DomainException {

    public BankTransactionNotFoundException(UUID bankTransactionId) {
        super("bank transaction " + bankTransactionId + " was not found");
    }
}
