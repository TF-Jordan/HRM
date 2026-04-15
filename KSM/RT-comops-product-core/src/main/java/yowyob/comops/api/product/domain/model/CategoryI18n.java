package yowyob.comops.api.product.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class CategoryI18n extends BaseEntity {

    private final UUID categoryId;
    private final String locale;
    private final String name;
    private final String description;

    private CategoryI18n(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID categoryId, String locale,
            String name, String description) {
        super(id, tenantId, createdAt, updatedAt);
        this.categoryId = requireUuid(categoryId, "categoryId");
        this.locale = requireText(locale, "locale").toLowerCase();
        this.name = requireText(name, "name");
        this.description = normalizeOptional(description);
    }

    public static CategoryI18n create(UUID tenantId, UUID categoryId, String locale, String name, String description) {
        Instant now = Instant.now();
        return new CategoryI18n(UUID.randomUUID(), tenantId, now, now, categoryId, locale, name, description);
    }

    public static CategoryI18n rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID categoryId,
            String locale, String name, String description) {
        return new CategoryI18n(id, tenantId, createdAt, updatedAt, categoryId, locale, name, description);
    }

    public UUID categoryId() { return categoryId; }
    public String locale() { return locale; }
    public String name() { return name; }
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
}
