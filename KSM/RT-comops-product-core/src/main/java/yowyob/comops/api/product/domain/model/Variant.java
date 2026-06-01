package yowyob.comops.api.product.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class Variant extends BaseEntity {

    private static final Set<String> ALLOWED_STATUSES = Set.of("ACTIVE", "INACTIVE");

    private final UUID productId;
    private final String sku;
    private final String barcode;
    private final String label;
    private final boolean isDefault;
    private final String status;

    private Variant(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID productId, String sku,
            String barcode, String label, boolean isDefault, String status) {
        super(id, tenantId, createdAt, updatedAt);
        this.productId = requireUuid(productId, "productId");
        this.sku = requireText(sku, "sku").toUpperCase();
        this.barcode = normalizeOptional(barcode);
        this.label = requireText(label, "label");
        this.isDefault = isDefault;
        this.status = normalizeStatus(status);
    }

    public static Variant create(UUID tenantId, UUID productId, String sku, String barcode, String label,
            boolean isDefault, String status) {
        Instant now = Instant.now();
        return new Variant(UUID.randomUUID(), tenantId, now, now, productId, sku, barcode, label, isDefault, status);
    }

    public static Variant rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID productId,
            String sku, String barcode, String label, boolean isDefault, String status) {
        return new Variant(id, tenantId, createdAt, updatedAt, productId, sku, barcode, label, isDefault, status);
    }

    public Variant update(String sku, String barcode, String label, boolean isDefault, String status) {
        return new Variant(id(), tenantId(), createdAt(), Instant.now(), productId, sku, barcode, label, isDefault,
                status);
    }

    public UUID productId() { return productId; }
    public String sku() { return sku; }
    public String barcode() { return barcode; }
    public String label() { return label; }
    public boolean isDefault() { return isDefault; }
    public String status() { return status; }

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

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String normalizeStatus(String value) {
        String normalized = requireText(value == null ? "ACTIVE" : value, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("status must be one of " + ALLOWED_STATUSES);
        }
        return normalized;
    }
}
