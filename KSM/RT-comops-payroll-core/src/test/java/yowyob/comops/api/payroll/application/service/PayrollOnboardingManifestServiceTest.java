package yowyob.comops.api.payroll.application.service;

import org.junit.jupiter.api.Test;

import yowyob.comops.api.payroll.application.port.in.PayrollOnboardingManifest;

import static org.assertj.core.api.Assertions.assertThat;

class PayrollOnboardingManifestServiceTest {

    private final PayrollOnboardingManifestService service = new PayrollOnboardingManifestService();

    @Test
    void manifestAdvertisesAllPayrollPermissions() {
        PayrollOnboardingManifest m = service.get();
        assertThat(m.module()).isEqualTo("payroll");
        assertThat(m.version()).isEqualTo("1");
        assertThat(m.permissions()).extracting(PayrollOnboardingManifest.PermissionDescriptor::code)
                .containsExactlyInAnyOrder("hrm:payroll:read", "hrm:payroll:run", "hrm:payroll:validate");
    }

    @Test
    void payrollAdminTemplateHasOnlyPayrollPermissions() {
        PayrollOnboardingManifest.RoleTemplate admin = service.get().suggestedRoleTemplates().stream()
                .filter(t -> t.code().equals("PAYROLL_ADMIN")).findFirst().orElseThrow();
        assertThat(admin.scopeType()).isEqualTo("ORGANIZATION");
        assertThat(admin.permissions())
                .allMatch(p -> p.startsWith("hrm:payroll:"))
                .contains("hrm:payroll:read", "hrm:payroll:run", "hrm:payroll:validate");
    }

    @Test
    void payrollEmployeeTemplateIsReadOnly() {
        PayrollOnboardingManifest.RoleTemplate emp = service.get().suggestedRoleTemplates().stream()
                .filter(t -> t.code().equals("PAYROLL_EMPLOYEE")).findFirst().orElseThrow();
        assertThat(emp.permissions()).containsExactly("hrm:payroll:read");
    }

    @Test
    void dataSourceModesCoverHrmAndLocal() {
        assertThat(service.get().dataSourceModes())
                .extracting(PayrollOnboardingManifest.DataSourceMode::code)
                .containsExactlyInAnyOrder("HRM", "LOCAL");
    }

    @Test
    void onboardingFlowPlacesDataSourceFlipAfterRoleAssignment() {
        var steps = service.get().onboardingFlow().steps();
        int assignIdx = steps.stream().filter(s -> s.endpoint().contains("/users/")).findFirst()
                .orElseThrow().order();
        int flipIdx = steps.stream().filter(s -> s.endpoint().contains("/data-source")).findFirst()
                .orElseThrow().order();
        assertThat(flipIdx).isGreaterThan(assignIdx);
    }
}
