package yowyob.comops.api.administration.adapter.in.web;

public record UpdateAdministrativePlatformOptionsRequest(
        boolean requireBusinessActorApproval,
        boolean requireOrganizationApproval,
        boolean allowOrganizationSelfServiceCreation,
        boolean allowAgencySelfServiceCreation,
        boolean allowRoleCloning,
        boolean allowAgencyScopedCustomRoles,
        boolean allowOrganizationAdminsToGovernAgencies,
        boolean allowBusinessActorSelfReactivation) {
}
