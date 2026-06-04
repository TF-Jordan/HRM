package yowyob.comops.api.payroll.application.service;

import yowyob.comops.api.payroll.domain.model.TerminationReason;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Period;
import java.util.EnumSet;
import java.util.Set;

/**
 * Computes an employee's final settlement (solde de tout compte) under Cameroonian rules.
 * Pure and stateless.
 *
 * <p>Components:
 * <ul>
 *   <li><b>Prorated salary</b> for the days worked in the departure month.</li>
 *   <li><b>Leave compensation</b>: unused leave days × daily rate (monthly salary / 30).</li>
 *   <li><b>Notice indemnity</b>: unserved notice months × monthly salary, on employer dismissal.</li>
 *   <li><b>Severance</b>: tiered on seniority — 20% of the monthly salary per year for years 1–5,
 *       25% for 6–10, 30% for 11–15, 35% beyond — due only after ≥ 2 years and only for
 *       eligible reasons (dismissal, retirement, death, mutual agreement); never for resignation
 *       or gross misconduct.</li>
 *   <li><b>Gratification</b>: any accrued 13th-month already earned (supplied by the caller).</li>
 * </ul>
 * Outstanding employer-loan balances are cleared from the gross settlement (capped at it).
 */
public final class FinalSettlementCalculator {

    private FinalSettlementCalculator() {}

    private static final Set<TerminationReason> SEVERANCE_ELIGIBLE = EnumSet.of(
            TerminationReason.DISMISSAL, TerminationReason.RETIREMENT,
            TerminationReason.DEATH, TerminationReason.MUTUAL_AGREEMENT);

    private static final int MIN_SEVERANCE_YEARS = 2;

    public static FinalSettlementResult calculate(FinalSettlementInput in) {
        BigDecimal salary = nz(in.referenceMonthlySalary());
        int seniority = seniorityYears(in);

        BigDecimal proratedSalary = ProrationCalculator.prorate(
                salary, in.finalMonthDaysWorked(), in.finalMonthDays());

        BigDecimal dailyRate = salary.divide(new BigDecimal("30"), 6, RoundingMode.HALF_UP);
        BigDecimal leaveCompensation = Rounding.money(nz(in.unusedLeaveDays()).multiply(dailyRate));

        BigDecimal noticeIndemnity = in.reason() == TerminationReason.DISMISSAL && in.noticeMonths() > 0
                ? Rounding.money(salary.multiply(BigDecimal.valueOf(in.noticeMonths())))
                : BigDecimal.ZERO;

        BigDecimal severance = severance(salary, seniority, in.reason());
        BigDecimal gratification = Rounding.money(nz(in.accruedGratification()));

        BigDecimal gross = proratedSalary.add(leaveCompensation).add(noticeIndemnity)
                .add(severance).add(gratification);

        BigDecimal loan = nz(in.outstandingLoanBalance());
        BigDecimal loanDeducted = loan.min(gross);
        BigDecimal net = gross.subtract(loanDeducted);

        return new FinalSettlementResult(proratedSalary, leaveCompensation, noticeIndemnity, severance,
                gratification, Rounding.money(gross), Rounding.money(loanDeducted), Rounding.money(net),
                seniority);
    }

    static int seniorityYears(FinalSettlementInput in) {
        if (in.hireDate() == null || in.departureDate() == null
                || in.departureDate().isBefore(in.hireDate())) {
            return 0;
        }
        return Period.between(in.hireDate(), in.departureDate()).getYears();
    }

    private static BigDecimal severance(BigDecimal salary, int seniorityYears, TerminationReason reason) {
        if (!SEVERANCE_ELIGIBLE.contains(reason) || seniorityYears < MIN_SEVERANCE_YEARS) {
            return BigDecimal.ZERO;
        }
        BigDecimal factor = BigDecimal.ZERO;
        for (int year = 1; year <= seniorityYears; year++) {
            factor = factor.add(tierRate(year));
        }
        return Rounding.money(salary.multiply(factor));
    }

    /** Severance rate for a given year of service (Cameroonian tiers). */
    private static BigDecimal tierRate(int year) {
        if (year <= 5) return new BigDecimal("0.20");
        if (year <= 10) return new BigDecimal("0.25");
        if (year <= 15) return new BigDecimal("0.30");
        return new BigDecimal("0.35");
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
