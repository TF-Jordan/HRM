package yowyob.comops.api.administration.adapter.in.web;

import yowyob.comops.api.roles.domain.model.Role;
import java.util.Set;
import java.util.UUID;

public record AdministrationRoleResponse(UUID id, UUID tenantId, String code, String name, String scopeType,
        Set<String> permissions) {
    public static AdministrationRoleResponse from(Role role) {
        return new AdministrationRoleResponse(role.id(), role.tenantId(), role.code(), role.name(),
                role.scopeType().name(), role.permissions());
    }
}
