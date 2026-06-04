package yowyob.comops.api.payroll.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Computes overtime pay from an hourly rate derived from the base salary and per-tier multipliers.
 *
 * Cameroonian Labour Code tiers (defaults the orchestration may pass): day +25% / +40%,
 * night +50%, Sunday &amp; public holidays +75%. The multipliers are the full pay factor for an
 * overtime hour (e.g. {@code 1.25} means the hour is paid at 125% of the base hourly rate).
 *
 * The legal monthly hours base (≈173.33 for a 40h week) is supplied by the caller so the rule
 * stays configurable per jurisdiction.
 */
public final class OvertimeCalculator {

    private OvertimeCalculator() {}

    /** Base hourly rate = monthly base salary / legal monthly hours. */
    public static BigDecimal hourlyRate(BigDecimal baseSalary, BigDecimal legalMonthlyHours) {
        if (baseSalary == null || legalMonthlyHours == null || legalMonthlyHours.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return baseSalary.divide(legalMonthlyHours, 6, RoundingMode.HALF_UP);
    }

    /**
     * Total overtime pay across the three tiers.
     *
     * @param baseSalary        monthly contractual base
     * @param legalMonthlyHours legal monthly hours (e.g. 173.33)
     * @param dayHours          overtime hours at the day multiplier
     * @param dayMultiplier     pay factor for day overtime (e.g. 1.25)
     * @param nightHours        overtime hours at the night multiplier
     * @param nightMultiplier   pay factor for night overtime (e.g. 1.50)
     * @param sundayHolidayHours overtime hours on Sundays/holidays
     * @param sundayMultiplier  pay factor for Sunday/holiday overtime (e.g. 1.75)
     */
    public static BigDecimal compute(BigDecimal baseSalary, BigDecimal legalMonthlyHours,
                                     BigDecimal dayHours, BigDecimal dayMultiplier,
                                     BigDecimal nightHours, BigDecimal nightMultiplier,
                                     BigDecimal sundayHolidayHours, BigDecimal sundayMultiplier) {
        BigDecimal rate = hourlyRate(baseSalary, legalMonthlyHours);
        if (rate.signum() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = tier(rate, dayHours, dayMultiplier)
                .add(tier(rate, nightHours, nightMultiplier))
                .add(tier(rate, sundayHolidayHours, sundayMultiplier));
        return Rounding.money(total);
    }

    private static BigDecimal tier(BigDecimal hourlyRate, BigDecimal hours, BigDecimal multiplier) {
        if (hours == null || multiplier == null || hours.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return hourlyRate.multiply(hours).multiply(multiplier);
    }
}
