package yowyob.comops.api.tp.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class ThirdPartyBankAccountAlreadyExistsException extends DomainException {
    public ThirdPartyBankAccountAlreadyExistsException(String iban) {
        super("Third-party bank account already exists: " + iban);
    }
}
