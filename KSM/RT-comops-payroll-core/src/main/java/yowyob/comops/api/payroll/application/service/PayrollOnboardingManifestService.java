package yowyob.comops.api.payroll.application.service;

import org.springframework.stereotype.Service;

import yowyob.comops.api.payroll.application.port.in.GetPayrollOnboardingManifestUseCase;
import yowyob.comops.api.payroll.application.port.in.PayrollOnboardingManifest;
import yowyob.comops.api.payroll.application.port.in.PayrollOnboardingManifest.DataSourceMode;
import yowyob.comops.api.payroll.application.port.in.PayrollOnboardingManifest.OnboardingFlow;
import yowyob.comops.api.payroll.application.port.in.PayrollOnboardingManifest.OnboardingStep;
import yowyob.comops.api.payroll.application.port.in.PayrollOnboardingManifest.PermissionDescriptor;
import yowyob.comops.api.payroll.application.port.in.PayrollOnboardingManifest.RoleTemplate;

import java.util.List;

/**
 * Builds the payroll onboarding manifest as an immutable singleton — pure data, no I/O.
 *
 * <p>The {@code PAYROLL_ADMIN} and {@code PAYROLL_EMPLOYEE} role templates carry <em>only</em>
 * the {@code hrm:payroll:*} permissions (the legacy prefix is preserved for backward
 * compatibility); no HRM permission is included, so a tenant subscribing to payroll alone
 * never gets a half-functional menu pointing at endpoints that aren't there.
 */
@Service
public class PayrollOnboardingManifestService implements GetPayrollOnboardingManifestUseCase {

    private static final String MANIFEST_VERSION = "1";

    @Override
    public PayrollOnboardingManifest get() {
        return new PayrollOnboardingManifest(
                "payroll",
                MANIFEST_VERSION,
                permissions(),
                roleTemplates(),
                dataSourceModes(),
                onboardingFlow());
    }

    private List<PermissionDescriptor> permissions() {
        return List.of(
                new PermissionDescriptor(
                        "hrm:payroll:read",
                        "Read payroll",
                        "List payroll cycles, entries, payslips, declarations and KPIs."),
                new PermissionDescriptor(
                        "hrm:payroll:run",
                        "Run payroll",
                        "Calculate / recalculate cycles, manage employees, variables, tax brackets and lookup tables."),
                new PermissionDescriptor(
                        "hrm:payroll:validate",
                        "Validate payroll",
                        "Validate / reject / approve a cycle, initiate payment, close the cycle."));
    }

    private List<RoleTemplate> roleTemplates() {
        List<String> adminPerms = List.of(
                "hrm:payroll:read", "hrm:payroll:run", "hrm:payroll:validate");
        List<String> employeePerms = List.of("hrm:payroll:read");
        return List.of(
                new RoleTemplate(
                        "PAYROLL_ADMIN",
                        "Payroll Administrator (standalone)",
                        "Full payroll control for a tenant subscribing to payroll without the HR module.",
                        "ORGANIZATION",
                        adminPerms),
                new RoleTemplate(
                        "PAYROLL_EMPLOYEE",
                        "Payroll Employee (self-service)",
                        "Self-service read access to one's own payslips and payment history.",
                        "ORGANIZATION",
                        employeePerms));
    }

    private List<DataSourceMode> dataSourceModes() {
        return List.of(
                new DataSourceMode(
                        "HRM",
                        "HR module",
                        "Employees, contracts, loans, leave and timesheets are read from hrm-core."),
                new DataSourceMode(
                        "LOCAL",
                        "Standalone payroll",
                        "Employees are stored in payroll-core's own table, populated via CSV import or CRUD."));
    }

    private OnboardingFlow onboardingFlow() {
        return new OnboardingFlow(List.of(
                new OnboardingStep(1,
                        "Create tenant + organization (administration-core).",
                        "/api/v1/administration/governance/organizations/{organizationId}",
                        "POST"),
                new OnboardingStep(2,
                        "Provision role templates from this manifest (administration-core).",
                        "/api/v1/administration/roles",
                        "POST"),
                new OnboardingStep(3,
                        "Assign the PAYROLL_ADMIN role to the first user.",
                        "/api/v1/administration/users/{userId}/roles",
                        "POST"),
                new OnboardingStep(4,
                        "Flip the organization payroll source to LOCAL.",
                        "/api/v1/payroll/employees/data-source",
                        "PUT"),
                new OnboardingStep(5,
                        "(optional) Import the initial employee CSV.",
                        "/api/v1/payroll/employees/import",
                        "POST")));
    }
}
