package yowyob.comops.api.payroll.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to compute a retroactive adjustment: recompute a past period for an employee with a
 * new base salary and record the delta to be paid in a later period.
 *
 * @param employeeId   employee concerned
 * @param originPeriod canonical {@code YYYY-MM} period being corrected (must have a regular run)
 * @param newBaseSalary corrected monthly base salary for that period
 * @param targetPeriod canonical {@code YYYY-MM} period in which the delta will be paid
 * @param reason       free-text justification (promotion, correction, CBA…)
 */
public record CalculateRetroactiveCommand(
        UUID employeeId,
        String originPeriod,
        BigDecimal newBaseSalary,
        String targetPeriod,
        String reason) {
}
