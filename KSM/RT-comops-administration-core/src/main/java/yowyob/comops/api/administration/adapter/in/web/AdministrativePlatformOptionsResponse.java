package yowyob.comops.api.administration.adapter.in.web;

import yowyob.comops.api.administration.domain.model.AdministrativePlatformOptions;
import java.util.UUID;

public record AdministrativePlatformOptionsResponse(
        UUID id,
        UUID tenantId,
        boolean requireBusinessActorApproval,
        boolean requireOrganizationApproval,
        boolean allowOrganizationSelfServiceCreation,
        boolean allowAgencySelfServiceCreation,
        boolean allowRoleCloning,
        boolean allowAgencyScopedCustomRoles,
        boolean allowOrganizationAdminsToGovernAgencies,
        boolean allowBusinessActorSelfReactivation) {

    public static AdministrativePlatformOptionsResponse from(AdministrativePlatformOptions options) {
        return new AdministrativePlatformOptionsResponse(options.id(), options.tenantId(),
                options.requireBusinessActorApproval(), options.requireOrganizationApproval(),
                options.allowOrganizationSelfServiceCreation(), options.allowAgencySelfServiceCreation(),
                options.allowRoleCloning(), options.allowAgencyScopedCustomRoles(),
                options.allowOrganizationAdminsToGovernAgencies(), options.allowBusinessActorSelfReactivation());
    }
}
