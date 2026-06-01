package yowyob.comops.api.accounting.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public class InvalidInvoiceStateException extends DomainException {

    public InvalidInvoiceStateException(UUID invoiceId, String currentStatus, String expectedStatus) {
        super("Invoice " + invoiceId + " is in status " + currentStatus + " but expected " + expectedStatus + ".");
    }
}
