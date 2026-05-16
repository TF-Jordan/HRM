package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class BusinessDomain extends BaseEntity {

    private final String code;
    private final String service;
    private final UUID parentId;
    private final String name;
    private final String imageUri;
    private final UUID imageId;
    private final String type;
    private final String typeLabel;
    private final String description;
    private final Instant deletedAt;

    private BusinessDomain(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, String code, String service,
            UUID parentId, String name, String imageUri, UUID imageId, String type, String typeLabel,
            String description, Instant deletedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.code = normalizeCode(code);
        this.service = normalizeOptional(service);
        this.parentId = parentId;
        this.name = requireText(name, "name");
        this.imageUri = normalizeOptional(imageUri);
        this.imageId = imageId;
        this.type = normalizeOptional(type);
        this.typeLabel = normalizeOptional(typeLabel);
        this.description = normalizeOptional(description);
        this.deletedAt = deletedAt;
    }

    public static BusinessDomain create(UUID tenantId, String code, String service, UUID parentId, String name,
            String imageUri, UUID imageId, String type, String typeLabel, String description) {
        Instant now = Instant.now();
        return new BusinessDomain(UUID.randomUUID(), tenantId, now, now, code, service, parentId, name, imageUri,
                imageId, type, typeLabel, description, null);
    }

    public static BusinessDomain rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, String code,
            String service, UUID parentId, String name, String imageUri, UUID imageId, String type, String typeLabel,
            String description, Instant deletedAt) {
        return new BusinessDomain(id, tenantId, createdAt, updatedAt, code, service, parentId, name, imageUri,
                imageId, type, typeLabel, description, deletedAt);
    }

    public String code() { return code; }
    public String service() { return service; }
    public UUID parentId() { return parentId; }
    public String name() { return name; }
    public String imageUri() { return imageUri; }
    public UUID imageId() { return imageId; }
    public String type() { return type; }
    public String typeLabel() { return typeLabel; }
    public String description() { return description; }
    public Instant deletedAt() { return deletedAt; }

    private static String normalizeCode(String value) {
        return requireText(value, "code").toUpperCase(Locale.ROOT);
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
