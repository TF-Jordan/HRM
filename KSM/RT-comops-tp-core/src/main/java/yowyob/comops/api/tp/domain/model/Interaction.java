package yowyob.comops.api.tp.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class Interaction extends BaseEntity {

    private final UUID interactionId;
    private final UUID prospectId;
    private final Instant interactionDate;
    private final String notes;
    private final Instant deletedAt;

    private Interaction(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID interactionId,
            UUID prospectId, Instant interactionDate, String notes, Instant deletedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.interactionId = interactionId;
        this.prospectId = prospectId;
        this.interactionDate = interactionDate;
        this.notes = normalizeOptional(notes);
        this.deletedAt = deletedAt;
    }

    public static Interaction create(UUID tenantId, UUID interactionId, UUID prospectId, Instant interactionDate,
            String notes) {
        Instant now = Instant.now();
        return new Interaction(UUID.randomUUID(), tenantId, now, now, interactionId, prospectId, interactionDate,
                notes, null);
    }

    public static Interaction rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID interactionId, UUID prospectId, Instant interactionDate, String notes, Instant deletedAt) {
        return new Interaction(id, tenantId, createdAt, updatedAt, interactionId, prospectId, interactionDate,
                notes, deletedAt);
    }

    public UUID interactionId() { return interactionId; }
    public UUID prospectId() { return prospectId; }
    public Instant interactionDate() { return interactionDate; }
    public String notes() { return notes; }
    public Instant deletedAt() { return deletedAt; }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
