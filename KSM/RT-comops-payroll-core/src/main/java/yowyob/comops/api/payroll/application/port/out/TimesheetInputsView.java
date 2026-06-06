package yowyob.comops.api.payroll.application.port.out;

import java.math.BigDecimal;

/**
 * Aggregated, payroll-relevant inputs derived from an employee's {@code VALIDATED} timesheets for
 * a period. Lets payroll-core consume declared-and-approved time without depending on hrm-core:
 * overtime hours are mapped onto the three legal tiers, and unjustified absences onto unpaid days.
 *
 * <ul>
 *   <li>{@code overtimeDayHours} — daytime overtime (paid at the day multiplier).</li>
 *   <li>{@code overtimeNightHours} — night overtime (night multiplier).</li>
 *   <li>{@code overtimeSundayHolidayHours} — Sunday/holiday overtime (Sunday multiplier).</li>
 *   <li>{@code unjustifiedAbsenceDays} — absence days that reduce the prorated base salary.</li>
 * </ul>
 */
public record TimesheetInputsView(
        BigDecimal overtimeDayHours,
        BigDecimal overtimeNightHours,
        BigDecimal overtimeSundayHolidayHours,
        BigDecimal unjustifiedAbsenceDays) {

    public static TimesheetInputsView empty() {
        return new TimesheetInputsView(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /** True when any overtime tier carries hours — used to decide whether timesheet OT applies. */
    public boolean hasOvertime() {
        return overtimeDayHours.add(overtimeNightHours).add(overtimeSundayHolidayHours).signum() > 0;
    }
}
