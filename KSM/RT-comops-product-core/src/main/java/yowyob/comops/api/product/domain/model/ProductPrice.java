package yowyob.comops.api.product.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ProductPrice extends BaseEntity {

    private final UUID productId;
    private final String priceType;
    private final BigDecimal amount;
    private final String currency;
    private final Instant effectiveFrom;

    private ProductPrice(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID productId, String priceType,
            BigDecimal amount, String currency, Instant effectiveFrom) {
        super(id, tenantId, createdAt, updatedAt);
        this.productId = requireUuid(productId, "productId");
        this.priceType = requireText(priceType, "priceType").toUpperCase();
        this.amount = requirePositive(amount, "amount");
        this.currency = requireText(currency, "currency").toUpperCase();
        this.effectiveFrom = Objects.requireNonNull(effectiveFrom, "effectiveFrom is required");
    }

    public static ProductPrice create(UUID tenantId, UUID productId, String priceType, BigDecimal amount, String currency,
            Instant effectiveFrom) {
        Instant now = Instant.now();
        return new ProductPrice(UUID.randomUUID(), tenantId, now, now, productId, priceType, amount, currency,
                effectiveFrom);
    }

    public static ProductPrice rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID productId,
            String priceType, BigDecimal amount, String currency, Instant effectiveFrom) {
        return new ProductPrice(id, tenantId, createdAt, updatedAt, productId, priceType, amount, currency,
                effectiveFrom);
    }

    public UUID productId() { return productId; }
    public String priceType() { return priceType; }
    public BigDecimal amount() { return amount; }
    public String currency() { return currency; }
    public Instant effectiveFrom() { return effectiveFrom; }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static BigDecimal requirePositive(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " is required");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }
}
