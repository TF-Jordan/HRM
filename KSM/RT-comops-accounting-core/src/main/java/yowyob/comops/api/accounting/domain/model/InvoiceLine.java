package yowyob.comops.api.accounting.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record InvoiceLine(UUID productId, BigDecimal quantity, BigDecimal unitPrice, BigDecimal lineAmount) {

    public InvoiceLine {
        Objects.requireNonNull(productId, "productId is required");
        Objects.requireNonNull(quantity, "quantity is required");
        Objects.requireNonNull(unitPrice, "unitPrice is required");
        Objects.requireNonNull(lineAmount, "lineAmount is required");
        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("unitPrice must be positive");
        }
        if (lineAmount.signum() <= 0) {
            throw new IllegalArgumentException("lineAmount must be positive");
        }
    }

    public static InvoiceLine create(UUID productId, BigDecimal quantity, BigDecimal unitPrice) {
        return new InvoiceLine(productId, quantity, unitPrice, quantity.multiply(unitPrice));
    }
}
