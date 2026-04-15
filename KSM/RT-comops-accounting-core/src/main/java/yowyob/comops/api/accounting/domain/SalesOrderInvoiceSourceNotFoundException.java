package yowyob.comops.api.accounting.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class SalesOrderInvoiceSourceNotFoundException extends DomainException {
    public SalesOrderInvoiceSourceNotFoundException(UUID orderId) {
        super("Confirmed sales order not available for invoicing: " + orderId);
    }
}
