package yowyob.comops.api.accounting.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class InvoiceAlreadyExistsForOrderException extends DomainException {
    public InvoiceAlreadyExistsForOrderException(UUID orderId) {
        super("An invoice already exists for sales order: " + orderId);
    }
}
