package yowyob.comops.api.payroll.application.service;

import java.math.BigDecimal;

/**
 * Outcome of a final-settlement computation, broken down into its statutory components.
 * All amounts are money-rounded.
 *
 * @param proratedSalary    salary for the days worked in the departure month
 * @param leaveCompensation cash-out of unused leave
 * @param noticeIndemnity   compensation for unserved notice (employer-side)
 * @param severanceIndemnity dismissal/retirement severance, per seniority tiers
 * @param gratification     accrued 13th-month / end-of-year bonus
 * @param grossSettlement   sum of the above
 * @param loanDeducted      outstanding loan balance cleared from the settlement
 * @param netSettlement     grossSettlement − loanDeducted
 * @param seniorityYears    completed years of service used for the severance tiers
 */
public record FinalSettlementResult(
        BigDecimal proratedSalary,
        BigDecimal leaveCompensation,
        BigDecimal noticeIndemnity,
        BigDecimal severanceIndemnity,
        BigDecimal gratification,
        BigDecimal grossSettlement,
        BigDecimal loanDeducted,
        BigDecimal netSettlement,
        int seniorityYears) {
}
