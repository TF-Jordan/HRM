package yowyob.comops.api.tp.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class ThirdPartyAccountingAccountAlreadyExistsException extends DomainException {
    public ThirdPartyAccountingAccountAlreadyExistsException(String accountingAccount) {
        super("Third-party accounting account already exists: " + accountingAccount);
    }
}
