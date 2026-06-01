package yowyob.comops.api.administration.adapter.in.web;

import yowyob.comops.api.roles.domain.model.UserRoleAssignment;
import java.util.UUID;

public record AdministrationUserRoleAssignmentResponse(UUID id, UUID tenantId, UUID userId, UUID roleId,
        String scopeType, UUID scopeId, String scope) {
    public static AdministrationUserRoleAssignmentResponse from(UserRoleAssignment assignment) {
        return new AdministrationUserRoleAssignmentResponse(assignment.id(), assignment.tenantId(), assignment.userId(),
                assignment.roleId(), assignment.scopeType().name(), assignment.scopeId(), assignment.scope());
    }
}
