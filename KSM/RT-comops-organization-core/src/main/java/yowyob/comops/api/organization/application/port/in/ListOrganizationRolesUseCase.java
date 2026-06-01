package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.application.port.out.OrganizationRoleGateway;
import reactor.core.publisher.Flux;

public interface ListOrganizationRolesUseCase {

    Flux<OrganizationRoleGateway.RoleRecord> listRoles();
}
