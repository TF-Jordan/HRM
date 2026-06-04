package yowyob.comops.api.payroll.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to capture (create or replace) an employee's variable inputs for a period.
 * The tenant is taken from the request context.
 *
 * @param period canonical {@code YYYY-MM} period
 */
public record CapturePayVariableCommand(
        UUID organizationId,
        UUID employeeId,
        String period,
        BigDecimal overtimeHoursDay,
        BigDecimal overtimeHoursNight,
        BigDecimal overtimeHoursSundayHoliday,
        BigDecimal bonuses,
        BigDecimal unpaidAbsenceDays,
        BigDecimal advances,
        Integer workedDaysOverride) {
}
