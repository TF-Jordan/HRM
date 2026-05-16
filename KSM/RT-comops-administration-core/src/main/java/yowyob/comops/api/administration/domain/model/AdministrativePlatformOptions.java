package yowyob.comops.api.administration.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class AdministrativePlatformOptions extends BaseEntity {

    private final boolean requireBusinessActorApproval;
    private final boolean requireOrganizationApproval;
    private final boolean allowOrganizationSelfServiceCreation;
    private final boolean allowAgencySelfServiceCreation;
    private final boolean allowRoleCloning;
    private final boolean allowAgencyScopedCustomRoles;
    private final boolean allowOrganizationAdminsToGovernAgencies;
    private final boolean allowBusinessActorSelfReactivation;

    private AdministrativePlatformOptions(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            boolean requireBusinessActorApproval, boolean requireOrganizationApproval,
            boolean allowOrganizationSelfServiceCreation, boolean allowAgencySelfServiceCreation,
            boolean allowRoleCloning, boolean allowAgencyScopedCustomRoles,
            boolean allowOrganizationAdminsToGovernAgencies, boolean allowBusinessActorSelfReactivation) {
        super(id, tenantId, createdAt, updatedAt);
        this.requireBusinessActorApproval = requireBusinessActorApproval;
        this.requireOrganizationApproval = requireOrganizationApproval;
        this.allowOrganizationSelfServiceCreation = allowOrganizationSelfServiceCreation;
        this.allowAgencySelfServiceCreation = allowAgencySelfServiceCreation;
        this.allowRoleCloning = allowRoleCloning;
        this.allowAgencyScopedCustomRoles = allowAgencyScopedCustomRoles;
        this.allowOrganizationAdminsToGovernAgencies = allowOrganizationAdminsToGovernAgencies;
        this.allowBusinessActorSelfReactivation = allowBusinessActorSelfReactivation;
    }

    public static AdministrativePlatformOptions defaults(UUID tenantId) {
        Instant now = Instant.now();
        return new AdministrativePlatformOptions(UUID.randomUUID(), tenantId, now, now,
                true, true, true, true, true, true, true, false);
    }

    public static AdministrativePlatformOptions rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            boolean requireBusinessActorApproval, boolean requireOrganizationApproval,
            boolean allowOrganizationSelfServiceCreation, boolean allowAgencySelfServiceCreation,
            boolean allowRoleCloning, boolean allowAgencyScopedCustomRoles,
            boolean allowOrganizationAdminsToGovernAgencies, boolean allowBusinessActorSelfReactivation) {
        return new AdministrativePlatformOptions(id, tenantId, createdAt, updatedAt,
                requireBusinessActorApproval, requireOrganizationApproval, allowOrganizationSelfServiceCreation,
                allowAgencySelfServiceCreation, allowRoleCloning, allowAgencyScopedCustomRoles,
                allowOrganizationAdminsToGovernAgencies, allowBusinessActorSelfReactivation);
    }

    public AdministrativePlatformOptions update(boolean requireBusinessActorApproval, boolean requireOrganizationApproval,
            boolean allowOrganizationSelfServiceCreation, boolean allowAgencySelfServiceCreation,
            boolean allowRoleCloning, boolean allowAgencyScopedCustomRoles,
            boolean allowOrganizationAdminsToGovernAgencies, boolean allowBusinessActorSelfReactivation) {
        return new AdministrativePlatformOptions(id(), tenantId(), createdAt(), Instant.now(),
                requireBusinessActorApproval, requireOrganizationApproval, allowOrganizationSelfServiceCreation,
                allowAgencySelfServiceCreation, allowRoleCloning, allowAgencyScopedCustomRoles,
                allowOrganizationAdminsToGovernAgencies, allowBusinessActorSelfReactivation);
    }

    public boolean requireBusinessActorApproval() { return requireBusinessActorApproval; }
    public boolean requireOrganizationApproval() { return requireOrganizationApproval; }
    public boolean allowOrganizationSelfServiceCreation() { return allowOrganizationSelfServiceCreation; }
    public boolean allowAgencySelfServiceCreation() { return allowAgencySelfServiceCreation; }
    public boolean allowRoleCloning() { return allowRoleCloning; }
    public boolean allowAgencyScopedCustomRoles() { return allowAgencyScopedCustomRoles; }
    public boolean allowOrganizationAdminsToGovernAgencies() { return allowOrganizationAdminsToGovernAgencies; }
    public boolean allowBusinessActorSelfReactivation() { return allowBusinessActorSelfReactivation; }
}
