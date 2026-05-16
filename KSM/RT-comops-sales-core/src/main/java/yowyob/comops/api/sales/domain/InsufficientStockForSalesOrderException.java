package yowyob.comops.api.sales.domain;

import java.math.BigDecimal;
import java.util.UUID;

public final class InsufficientStockForSalesOrderException extends RuntimeException {

    public InsufficientStockForSalesOrderException(UUID orderId, UUID productId, BigDecimal requiredQuantity,
            BigDecimal availableQuantity) {
        super("Cannot confirm sales order " + orderId + " because stock is insufficient for product " + productId
                + ". required=" + requiredQuantity + ", available=" + availableQuantity);
    }
}
