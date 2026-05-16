package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class OpeningHoursExceptionRule extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final LocalDate exceptionDate;
    private final String label;
    private final LocalTime opensAt;
    private final LocalTime closesAt;
    private final boolean closed;

    private OpeningHoursExceptionRule(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID agencyId, LocalDate exceptionDate, String label, LocalTime opensAt, LocalTime closesAt,
            boolean closed) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.agencyId = requireUuid(agencyId, "agencyId");
        this.exceptionDate = java.util.Objects.requireNonNull(exceptionDate, "exceptionDate is required");
        this.label = label == null || label.isBlank() ? null : label.trim();
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.closed = closed;
        validateWindow(closed, opensAt, closesAt);
    }

    public static OpeningHoursExceptionRule create(UUID tenantId, UUID organizationId, UUID agencyId,
            LocalDate exceptionDate, String label, LocalTime opensAt, LocalTime closesAt, boolean closed) {
        Instant now = Instant.now();
        return new OpeningHoursExceptionRule(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId,
                exceptionDate, label, opensAt, closesAt, closed);
    }

    public static OpeningHoursExceptionRule rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, LocalDate exceptionDate, String label, LocalTime opensAt,
            LocalTime closesAt, boolean closed) {
        return new OpeningHoursExceptionRule(id, tenantId, createdAt, updatedAt, organizationId, agencyId,
                exceptionDate, label, opensAt, closesAt, closed);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public LocalDate exceptionDate() { return exceptionDate; }
    public String label() { return label; }
    public LocalTime opensAt() { return opensAt; }
    public LocalTime closesAt() { return closesAt; }
    public boolean closed() { return closed; }

    public boolean appliesAt(java.time.LocalDateTime dateTime) {
        return exceptionDate.equals(dateTime.toLocalDate())
                && (closed || (!dateTime.toLocalTime().isBefore(opensAt) && dateTime.toLocalTime().isBefore(closesAt)));
    }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static void validateWindow(boolean closed, LocalTime opensAt, LocalTime closesAt) {
        if (closed) {
            if (opensAt != null || closesAt != null) {
                throw new IllegalArgumentException("opensAt and closesAt must be null when exception marks agency closed");
            }
            return;
        }
        if (opensAt == null || closesAt == null || !opensAt.isBefore(closesAt)) {
            throw new IllegalArgumentException("valid opening interval is required for opening hours exception");
        }
    }
}
