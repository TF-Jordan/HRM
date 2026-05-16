package yowyob.comops.api.product.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class VariantPrice extends BaseEntity {

    private final UUID variantId;
    private final String priceType;
    private final BigDecimal amount;
    private final String currency;
    private final Instant effectiveFrom;

    private VariantPrice(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID variantId, String priceType,
            BigDecimal amount, String currency, Instant effectiveFrom) {
        super(id, tenantId, createdAt, updatedAt);
        this.variantId = requireUuid(variantId, "variantId");
        this.priceType = requireText(priceType, "priceType").toUpperCase();
        this.amount = requirePositive(amount, "amount");
        this.currency = requireText(currency, "currency").toUpperCase();
        this.effectiveFrom = effectiveFrom == null ? Instant.now() : effectiveFrom;
    }

    public static VariantPrice create(UUID tenantId, UUID variantId, String priceType, BigDecimal amount, String currency,
            Instant effectiveFrom) {
        Instant now = Instant.now();
        return new VariantPrice(UUID.randomUUID(), tenantId, now, now, variantId, priceType, amount, currency,
                effectiveFrom);
    }

    public static VariantPrice rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID variantId,
            String priceType, BigDecimal amount, String currency, Instant effectiveFrom) {
        return new VariantPrice(id, tenantId, createdAt, updatedAt, variantId, priceType, amount, currency,
                effectiveFrom);
    }

    public UUID variantId() { return variantId; }
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
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }
}
