package yowyob.comops.api.accounting.adapter.in.web;

import yowyob.comops.api.accounting.domain.model.InvoiceLine;
import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceLineResponse(UUID productId, BigDecimal quantity, BigDecimal unitPrice, BigDecimal lineAmount) {

    public static InvoiceLineResponse from(InvoiceLine line) {
        return new InvoiceLineResponse(line.productId(), line.quantity(), line.unitPrice(), line.lineAmount());
    }
}
