package yowyob.comops.api.product.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class MediaAsset extends BaseEntity {

    private final String targetType;
    private final UUID targetId;
    private final UUID fileId;
    private final String mimeType;
    private final int position;
    private final String altText;

    private MediaAsset(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, String targetType, UUID targetId,
            UUID fileId, String mimeType, int position, String altText) {
        super(id, tenantId, createdAt, updatedAt);
        this.targetType = requireText(targetType, "targetType").toUpperCase();
        this.targetId = requireUuid(targetId, "targetId");
        this.fileId = requireUuid(fileId, "fileId");
        this.mimeType = requireText(mimeType, "mimeType");
        this.position = requirePositive(position);
        this.altText = normalizeOptional(altText);
    }

    public static MediaAsset create(UUID tenantId, String targetType, UUID targetId, UUID fileId, String mimeType,
            int position, String altText) {
        Instant now = Instant.now();
        return new MediaAsset(UUID.randomUUID(), tenantId, now, now, targetType, targetId, fileId, mimeType, position,
                altText);
    }

    public static MediaAsset rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, String targetType,
            UUID targetId, UUID fileId, String mimeType, int position, String altText) {
        return new MediaAsset(id, tenantId, createdAt, updatedAt, targetType, targetId, fileId, mimeType, position,
                altText);
    }

    public String targetType() { return targetType; }
    public UUID targetId() { return targetId; }
    public UUID fileId() { return fileId; }
    public String mimeType() { return mimeType; }
    public int position() { return position; }
    public String altText() { return altText; }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static int requirePositive(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("position must be positive");
        }
        return position;
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
