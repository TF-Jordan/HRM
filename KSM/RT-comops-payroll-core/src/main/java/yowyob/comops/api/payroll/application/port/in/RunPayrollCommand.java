package yowyob.comops.api.payroll.application.port.in;

import yowyob.comops.api.payroll.domain.model.RunType;

import java.util.UUID;

/**
 * Command to launch a payroll cycle. Tenant and organization come from the request context.
 *
 * @param period   canonical {@code YYYY-MM} period
 * @param agencyId restrict to one agency, or {@code null} for the whole organization
 * @param runType  nature of the run (defaults to REGULAR when null)
 */
public record RunPayrollCommand(String period, UUID agencyId, RunType runType) {

    public RunType runTypeOrDefault() {
        return runType == null ? RunType.REGULAR : runType;
    }
}
