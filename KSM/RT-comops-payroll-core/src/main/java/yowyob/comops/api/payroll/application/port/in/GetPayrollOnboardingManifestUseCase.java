package yowyob.comops.api.payroll.application.port.in;

/**
 * Returns the static, self-describing manifest of payroll-core capabilities so other modules
 * (notably administration-core's onboarding pipeline) don't have to hard-code our permissions
 * or role layouts.
 */
public interface GetPayrollOnboardingManifestUseCase {

    PayrollOnboardingManifest get();
}
