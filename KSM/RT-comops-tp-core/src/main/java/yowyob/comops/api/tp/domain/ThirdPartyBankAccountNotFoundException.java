package yowyob.comops.api.tp.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class ThirdPartyBankAccountNotFoundException extends DomainException {
    public ThirdPartyBankAccountNotFoundException(UUID bankAccountId) {
        super("Third-party bank account not found: " + bankAccountId);
    }
}
