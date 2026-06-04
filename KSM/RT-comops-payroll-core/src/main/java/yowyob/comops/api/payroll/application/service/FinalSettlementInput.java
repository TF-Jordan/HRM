package yowyob.comops.api.payroll.application.service;

import yowyob.comops.api.payroll.domain.model.TerminationReason;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Inputs to the final-settlement (solde de tout compte) computation for one departing employee.
 *
 * @param referenceMonthlySalary reference monthly salary (typically the 12-month average; the
 *                               caller supplies it) used as the base for indemnities
 * @param hireDate               employment start, for seniority
 * @param departureDate          last day of employment
 * @param reason                 termination reason, drives severance/notice eligibility
 * @param unusedLeaveDays        accrued but untaken leave days to be cashed out
 * @param finalMonthDaysWorked   days worked in the departure month (for the prorated salary)
 * @param finalMonthDays         number of days in the departure month
 * @param noticeMonths           contractual notice period in months not served by the employee
 *                               (paid by the employer on dismissal); 0 if notice was served
 * @param accruedGratification   prorated 13th-month / end-of-year bonus already earned, or zero
 * @param outstandingLoanBalance remaining employer-loan balance to clear from the settlement
 */
public record FinalSettlementInput(
        BigDecimal referenceMonthlySalary,
        LocalDate hireDate,
        LocalDate departureDate,
        TerminationReason reason,
        BigDecimal unusedLeaveDays,
        int finalMonthDaysWorked,
        int finalMonthDays,
        int noticeMonths,
        BigDecimal accruedGratification,
        BigDecimal outstandingLoanBalance) {
}
