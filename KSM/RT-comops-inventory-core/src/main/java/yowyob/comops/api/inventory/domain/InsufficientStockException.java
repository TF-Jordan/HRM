package yowyob.comops.api.inventory.domain;

import java.math.BigDecimal;
import java.util.UUID;

public final class InsufficientStockException extends RuntimeException {

    private final UUID productId;
    private final UUID agencyId;
    private final BigDecimal requiredQuantity;
    private final BigDecimal availableQuantity;

    public InsufficientStockException(UUID productId, UUID agencyId, BigDecimal requiredQuantity, BigDecimal availableQuantity) {
        super("Insufficient stock for product " + productId + " in agency " + agencyId
                + ". required=" + requiredQuantity + ", available=" + availableQuantity);
        this.productId = productId;
        this.agencyId = agencyId;
        this.requiredQuantity = requiredQuantity;
        this.availableQuantity = availableQuantity;
    }

    public UUID productId() {
        return productId;
    }

    public UUID agencyId() {
        return agencyId;
    }

    public BigDecimal requiredQuantity() {
        return requiredQuantity;
    }

    public BigDecimal availableQuantity() {
        return availableQuantity;
    }
}
