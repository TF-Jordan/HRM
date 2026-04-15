package yowyob.comops.api.administration.application.port.in;

import java.util.UUID;

public record UpdateAdministrativePlatformOptionsCommand(
        UUID tenantId,
        UUID organizationId,
        UUID actorUserId,
        boolean requireBusinessActorApproval,
        boolean requireOrganizationApproval,
        boolean allowOrganizationSelfServiceCreation,
        boolean allowAgencySelfServiceCreation,
        boolean allowRoleCloning,
        boolean allowAgencyScopedCustomRoles,
        boolean allowOrganizationAdminsToGovernAgencies,
        boolean allowBusinessActorSelfReactivation) {
}
