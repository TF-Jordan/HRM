package yowyob.comops.api.treasury.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class BankAccountNotFoundException extends DomainException {
    public BankAccountNotFoundException(UUID bankAccountId) {
        super("Bank account not found: " + bankAccountId);
    }
}
