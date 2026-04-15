package yowyob.comops.api.treasury.domain;

import java.util.UUID;

public final class InvoiceSettlementNotFoundException extends RuntimeException {

    public InvoiceSettlementNotFoundException(UUID settlementId) {
        super("Invoice settlement not found: " + settlementId);
    }
}
