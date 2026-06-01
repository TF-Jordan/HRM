package yowyob.comops.api.treasury.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class DuplicateBankTransactionReferenceException extends DomainException {

    public DuplicateBankTransactionReferenceException(String referenceNumber) {
        super("bank transaction reference '" + referenceNumber + "' already exists");
    }
}
