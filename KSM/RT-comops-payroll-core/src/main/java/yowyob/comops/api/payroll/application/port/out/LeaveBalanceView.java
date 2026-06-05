package yowyob.comops.api.payroll.application.port.out;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Annual leave balance read from HR for the current year.
 *
 * @param acquired       leave days accrued so far this year
 * @param taken          leave days already taken
 * @param remaining      leave days still available (the cash-out base for the STC)
 */
public record LeaveBalanceView(BigDecimal acquired, BigDecimal taken, BigDecimal remaining) {

    public LeaveBalanceView {
        Objects.requireNonNull(acquired, "acquired is required");
        Objects.requireNonNull(taken, "taken is required");
        Objects.requireNonNull(remaining, "remaining is required");
    }

    public static LeaveBalanceView empty() {
        return new LeaveBalanceView(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
