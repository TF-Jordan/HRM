package yowyob.comops.api.accounting.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class InvoiceNotFoundException extends DomainException {
    public InvoiceNotFoundException(UUID invoiceId) { super("Invoice not found: " + invoiceId); }
}
