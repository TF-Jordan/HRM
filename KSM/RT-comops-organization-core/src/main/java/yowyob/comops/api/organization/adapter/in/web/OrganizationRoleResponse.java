package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.application.port.out.OrganizationRoleGateway;
import java.util.List;
import java.util.UUID;

public record OrganizationRoleResponse(UUID id, String code, String name, List<String> permissions) {

    public static OrganizationRoleResponse from(OrganizationRoleGateway.RoleRecord role) {
        return new OrganizationRoleResponse(role.id(), role.code(), role.name(), role.permissions());
    }
}
