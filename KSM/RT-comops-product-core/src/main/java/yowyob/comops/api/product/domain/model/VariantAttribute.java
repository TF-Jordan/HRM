package yowyob.comops.api.product.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class VariantAttribute extends BaseEntity {

    private final UUID variantId;
    private final String attributeName;
    private final String attributeValue;

    private VariantAttribute(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID variantId,
            String attributeName, String attributeValue) {
        super(id, tenantId, createdAt, updatedAt);
        this.variantId = requireUuid(variantId, "variantId");
        this.attributeName = requireText(attributeName, "attributeName");
        this.attributeValue = requireText(attributeValue, "attributeValue");
    }

    public static VariantAttribute create(UUID tenantId, UUID variantId, String attributeName, String attributeValue) {
        Instant now = Instant.now();
        return new VariantAttribute(UUID.randomUUID(), tenantId, now, now, variantId, attributeName, attributeValue);
    }

    public static VariantAttribute rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID variantId,
            String attributeName, String attributeValue) {
        return new VariantAttribute(id, tenantId, createdAt, updatedAt, variantId, attributeName, attributeValue);
    }

    public UUID variantId() { return variantId; }
    public String attributeName() { return attributeName; }
    public String attributeValue() { return attributeValue; }

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
}
