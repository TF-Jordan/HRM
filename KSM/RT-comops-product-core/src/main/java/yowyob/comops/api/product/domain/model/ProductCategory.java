package yowyob.comops.api.product.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class ProductCategory extends BaseEntity {

    private final UUID organizationId;
    private final String code;
    private final String name;
    private final String parentCode;
    private final String description;

    private ProductCategory(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId, String code,
            String name, String parentCode, String description) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.code = requireText(code, "code").toUpperCase();
        this.name = requireText(name, "name");
        this.parentCode = normalizeOptionalCode(parentCode);
        this.description = normalizeOptional(description);
    }

    public static ProductCategory create(UUID tenantId, UUID organizationId, String code, String name, String parentCode,
            String description) {
        Instant now = Instant.now();
        return new ProductCategory(UUID.randomUUID(), tenantId, now, now, organizationId, code, name, parentCode,
                description);
    }

    public static ProductCategory rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, String code, String name, String parentCode, String description) {
        return new ProductCategory(id, tenantId, createdAt, updatedAt, organizationId, code, name, parentCode,
                description);
    }

    public UUID organizationId() { return organizationId; }
    public String code() { return code; }
    public String name() { return name; }
    public String parentCode() { return parentCode; }
    public String description() { return description; }

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

    private static String normalizeOptionalCode(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }
}
