package yowyob.comops.api.sales.adapter.in.web;

import yowyob.comops.api.sales.domain.model.SalesOrder;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SalesOrderResponse(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        UUID customerThirdPartyId,
        UUID productId,
        String orderNumber,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalQuantity,
        BigDecimal subtotalAmount,
        BigDecimal totalAmount,
        String currency,
        String status,
        List<SalesOrderLineResponse> lines) {

    public static SalesOrderResponse from(SalesOrder salesOrder) {
        return new SalesOrderResponse(salesOrder.id(), salesOrder.tenantId(), salesOrder.organizationId(),
                salesOrder.agencyId(), salesOrder.customerThirdPartyId(), salesOrder.productId(),
                salesOrder.orderNumber(), salesOrder.quantity(), salesOrder.unitPrice(), salesOrder.totalQuantity(),
                salesOrder.subtotalAmount(), salesOrder.totalAmount(), salesOrder.currency(), salesOrder.status(),
                salesOrder.lines().stream().map(SalesOrderLineResponse::from).toList());
    }
}
