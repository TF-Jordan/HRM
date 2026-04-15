package yowyob.comops.api.accounting.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class DuplicateInvoiceNumberException extends DomainException {

    public DuplicateInvoiceNumberException(String invoiceNumber) {
        super("An invoice already exists with number: " + invoiceNumber);
    }
}
