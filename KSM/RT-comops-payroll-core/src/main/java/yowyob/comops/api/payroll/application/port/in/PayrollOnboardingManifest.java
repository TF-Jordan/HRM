package yowyob.comops.api.payroll.application.port.in;

import java.util.List;

/**
 * Self-describing payroll-core capabilities consumed by administration-core's onboarding
 * pipeline so it can provision a tenant for <em>standalone payroll</em> without hard-coding
 * our internals.
 *
 * The manifest is read-only and side-effect-free: administration-core fetches it once when
 * a tenant subscribes to the payroll service, then uses the values to (1) seed permissions
 * in its catalog, (2) provision suggested role templates via its existing role APIs, and
 * (3) call our {@code PUT /data-source} endpoint to flip the organization to LOCAL.
 */
public record PayrollOnboardingManifest(
        String module,
        String version,
        List<PermissionDescriptor> permissions,
        List<RoleTemplate> suggestedRoleTemplates,
        List<DataSourceMode> dataSourceModes,
        OnboardingFlow onboardingFlow) {

    /** A payroll permission with the label administration-core should display in its catalog. */
    public record PermissionDescriptor(String code, String label, String description) {}

    /**
     * A role template payroll-core suggests for standalone tenants. administration-core can
     * provision it verbatim through its {@code POST /roles} endpoint without referencing
     * HRM permissions that wouldn't resolve in standalone mode.
     */
    public record RoleTemplate(
            String code,
            String name,
            String description,
            String scopeType,
            List<String> permissions) {}

    public record DataSourceMode(String code, String label, String description) {}

    /** Ordered list of steps administration-core walks through to onboard a payroll-only tenant. */
    public record OnboardingFlow(List<OnboardingStep> steps) {}

    public record OnboardingStep(int order, String description, String endpoint, String method) {}
}
