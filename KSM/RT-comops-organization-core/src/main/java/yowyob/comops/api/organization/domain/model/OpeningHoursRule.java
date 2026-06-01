package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public final class OpeningHoursRule extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final DayOfWeek dayOfWeek;
    private final LocalTime opensAt;
    private final LocalTime closesAt;
    private final boolean closed;

    private OpeningHoursRule(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId, UUID agencyId,
            DayOfWeek dayOfWeek, LocalTime opensAt, LocalTime closesAt, boolean closed) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.agencyId = requireUuid(agencyId, "agencyId");
        this.dayOfWeek = java.util.Objects.requireNonNull(dayOfWeek, "dayOfWeek is required");
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.closed = closed;
        validateOpeningWindow(closed, opensAt, closesAt);
    }

    public static OpeningHoursRule create(UUID tenantId, UUID organizationId, UUID agencyId, DayOfWeek dayOfWeek,
            LocalTime opensAt, LocalTime closesAt, boolean closed) {
        Instant now = Instant.now();
        return new OpeningHoursRule(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, dayOfWeek, opensAt,
                closesAt, closed);
    }

    public static OpeningHoursRule rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, DayOfWeek dayOfWeek, LocalTime opensAt, LocalTime closesAt,
            boolean closed) {
        return new OpeningHoursRule(id, tenantId, createdAt, updatedAt, organizationId, agencyId, dayOfWeek, opensAt,
                closesAt, closed);
    }

    public OpeningHoursRule update(LocalTime opensAt, LocalTime closesAt, boolean closed) {
        return new OpeningHoursRule(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId, dayOfWeek,
                opensAt, closesAt, closed);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public DayOfWeek dayOfWeek() { return dayOfWeek; }
    public LocalTime opensAt() { return opensAt; }
    public LocalTime closesAt() { return closesAt; }
    public boolean closed() { return closed; }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " is required");
        return value;
    }

    private static void validateOpeningWindow(boolean closed, LocalTime opensAt, LocalTime closesAt) {
        if (closed) {
            if (opensAt != null || closesAt != null) {
                throw new IllegalArgumentException("opensAt and closesAt must be null when agency is closed");
            }
            return;
        }
        if (opensAt == null || closesAt == null || !opensAt.isBefore(closesAt)) {
            throw new IllegalArgumentException("valid opening interval is required when agency is open");
        }
    }
}
