package yowyob.comops.api.product.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class ProductSpec extends BaseEntity {

    private final UUID productId;
    private final BigDecimal weightKg;
    private final BigDecimal lengthCm;
    private final BigDecimal widthCm;
    private final BigDecimal heightCm;
    private final String materials;

    private ProductSpec(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID productId, BigDecimal weightKg,
            BigDecimal lengthCm, BigDecimal widthCm, BigDecimal heightCm, String materials) {
        super(id, tenantId, createdAt, updatedAt);
        this.productId = requireUuid(productId, "productId");
        this.weightKg = normalizeDecimal(weightKg);
        this.lengthCm = normalizeDecimal(lengthCm);
        this.widthCm = normalizeDecimal(widthCm);
        this.heightCm = normalizeDecimal(heightCm);
        this.materials = normalizeOptional(materials);
    }

    public static ProductSpec create(UUID tenantId, UUID productId, BigDecimal weightKg, BigDecimal lengthCm,
            BigDecimal widthCm, BigDecimal heightCm, String materials) {
        Instant now = Instant.now();
        return new ProductSpec(UUID.randomUUID(), tenantId, now, now, productId, weightKg, lengthCm, widthCm, heightCm,
                materials);
    }

    public static ProductSpec rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID productId,
            BigDecimal weightKg, BigDecimal lengthCm, BigDecimal widthCm, BigDecimal heightCm, String materials) {
        return new ProductSpec(id, tenantId, createdAt, updatedAt, productId, weightKg, lengthCm, widthCm, heightCm,
                materials);
    }

    public ProductSpec update(BigDecimal weightKg, BigDecimal lengthCm, BigDecimal widthCm, BigDecimal heightCm,
            String materials) {
        return new ProductSpec(id(), tenantId(), createdAt(), Instant.now(), productId, weightKg, lengthCm, widthCm,
                heightCm, materials);
    }

    public UUID productId() { return productId; }
    public BigDecimal weightKg() { return weightKg; }
    public BigDecimal lengthCm() { return lengthCm; }
    public BigDecimal widthCm() { return widthCm; }
    public BigDecimal heightCm() { return heightCm; }
    public String materials() { return materials; }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static BigDecimal normalizeDecimal(BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException("dimensions and weight must be positive");
        }
        return value;
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
