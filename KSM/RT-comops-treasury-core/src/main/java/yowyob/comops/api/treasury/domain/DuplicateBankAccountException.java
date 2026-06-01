package yowyob.comops.api.treasury.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class DuplicateBankAccountException extends DomainException {

    public DuplicateBankAccountException(String accountNumber) {
        super("A bank account already exists with account number: " + accountNumber);
    }
}
