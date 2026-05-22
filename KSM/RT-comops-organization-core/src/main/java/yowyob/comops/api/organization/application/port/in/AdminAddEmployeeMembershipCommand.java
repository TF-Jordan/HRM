package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;

/**
 * Admin-grade direct membership creation used during automated user
 * provisioning (e.g. when an HRM Employee is created together with a
 * connectable auth account). Unlike {@link InviteEmployeeCommand}, this
 * command carries the resolved {@code userId} + {@code actorId} so no
 * email lookup is required and bypasses the "discover existing account
 * by email" step.
 */
public record AdminAddEmployeeMembershipCommand(
        UUID tenantId,
        UUID organizationId,
        UUID userId,
        UUID actorId,
        String email,
        UUID agencyId,
        UUID roleId) {
}
