package yowyob.comops.api.payroll.application.port.in;

import yowyob.comops.api.payroll.domain.model.TerminationReason;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to compute a departing employee's final settlement. The employee's salary, hire date
 * and outstanding loans are resolved from HR; the caller supplies the departure-specific inputs.
 *
 * @param employeeId           departing employee
 * @param departureDate        last day of employment (also fixes the settlement period/month)
 * @param reason               termination reason (drives severance/notice eligibility)
 * @param unusedLeaveDays      accrued but untaken leave days to cash out
 * @param noticeMonths         unserved notice months paid by the employer (dismissal), else 0
 * @param accruedGratification prorated 13th-month already earned, or zero
 */
public record CalculateFinalSettlementCommand(
        UUID employeeId,
        LocalDate departureDate,
        TerminationReason reason,
        BigDecimal unusedLeaveDays,
        int noticeMonths,
        BigDecimal accruedGratification) {
}
