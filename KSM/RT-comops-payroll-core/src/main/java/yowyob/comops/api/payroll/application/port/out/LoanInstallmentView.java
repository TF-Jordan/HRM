package yowyob.comops.api.payroll.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A payroll-owned projection of an active employer loan/advance installment to be deducted
 * from net pay. The HR module owns the loan lifecycle; payroll only reads the due installment
 * and reports back the amount it withheld via {@link HrmEmployeeDataPort#registerLoanDeduction}.
 *
 * @param loanId            HR loan/advance identifier
 * @param employeeId        owning employee
 * @param monthlyInstallment installment due this period
 * @param remainingBalance  outstanding balance before this period's deduction
 */
public record LoanInstallmentView(
        UUID loanId,
        UUID employeeId,
        BigDecimal monthlyInstallment,
        BigDecimal remainingBalance) {
}
