package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class EmployeeMembership extends BaseEntity {

    private final UUID organizationId;
    private final UUID userId;
    private final UUID actorId;
    private final String email;
    private final UUID agencyId;
    private final UUID roleId;
    private final String status;

    private EmployeeMembership(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID userId, UUID actorId, String email, UUID agencyId, UUID roleId, String status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.userId = requireUuid(userId, "userId");
        this.actorId = actorId;
        this.email = requireText(email, "email").toLowerCase();
        this.agencyId = agencyId;
        this.roleId = roleId;
        this.status = requireText(status, "status").toUpperCase();
    }

    public static EmployeeMembership invite(UUID tenantId, UUID organizationId, UUID userId, UUID actorId, String email,
            UUID agencyId, UUID roleId) {
        Instant now = Instant.now();
        return new EmployeeMembership(UUID.randomUUID(), tenantId, now, now, organizationId, userId, actorId, email,
                agencyId, roleId, "ACTIVE");
    }

    public static EmployeeMembership rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID userId, UUID actorId, String email, UUID agencyId, UUID roleId, String status) {
        return new EmployeeMembership(id, tenantId, createdAt, updatedAt, organizationId, userId, actorId, email,
                agencyId, roleId, status);
    }

    public EmployeeMembership remove() {
        return new EmployeeMembership(id(), tenantId(), createdAt(), Instant.now(), organizationId, userId, actorId, email,
                agencyId, roleId, "REMOVED");
    }

    public UUID organizationId() { return organizationId; }
    public UUID userId() { return userId; }
    public UUID actorId() { return actorId; }
    public String email() { return email; }
    public UUID agencyId() { return agencyId; }
    public UUID roleId() { return roleId; }
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
}
