package yowyob.comops.api.sales.adapter.in.web;

import yowyob.comops.api.sales.domain.model.SalesOrderLine;
import java.math.BigDecimal;
import java.util.UUID;

public record SalesOrderLineResponse(UUID productId, BigDecimal quantity, BigDecimal unitPrice, BigDecimal lineAmount) {

    public static SalesOrderLineResponse from(SalesOrderLine line) {
        return new SalesOrderLineResponse(line.productId(), line.quantity(), line.unitPrice(), line.lineAmount());
    }
}
