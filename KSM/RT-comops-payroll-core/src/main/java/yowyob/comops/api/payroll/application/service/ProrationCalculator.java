package yowyob.comops.api.payroll.application.service;

import yowyob.comops.api.payroll.domain.model.PayPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Computes time-proration of the base salary for mid-month arrivals and departures.
 *
 * Uses the calendar-day method: {@code prorated = base * workedDays / daysInMonth}. An explicit
 * worked-days override (captured as a pay variable) takes precedence over the hire/departure dates.
 */
public final class ProrationCalculator {

    private ProrationCalculator() {}

    /**
     * Number of calendar days the employee is present within the period, bounded by hire and
     * departure dates. Returns the full month length when the employee spans the whole period.
     */
    public static int workedDays(PayPeriod period, LocalDate hireDate, LocalDate departureDate) {
        LocalDate periodStart = period.firstDay();
        LocalDate periodEnd = period.lastDay();

        LocalDate start = (hireDate != null && hireDate.isAfter(periodStart)) ? hireDate : periodStart;
        LocalDate end = (departureDate != null && departureDate.isBefore(periodEnd)) ? departureDate : periodEnd;

        if (end.isBefore(start)) {
            return 0;
        }
        return (int) (end.toEpochDay() - start.toEpochDay() + 1);
    }

    /** Prorated base salary for the given worked days over the period length. */
    public static BigDecimal prorate(BigDecimal baseSalary, int workedDays, int daysInMonth) {
        if (baseSalary == null || daysInMonth <= 0) {
            return BigDecimal.ZERO;
        }
        if (workedDays >= daysInMonth) {
            return Rounding.money(baseSalary);
        }
        if (workedDays <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal factor = BigDecimal.valueOf(workedDays)
                .divide(BigDecimal.valueOf(daysInMonth), 10, RoundingMode.HALF_UP);
        return Rounding.money(baseSalary.multiply(factor));
    }
}
