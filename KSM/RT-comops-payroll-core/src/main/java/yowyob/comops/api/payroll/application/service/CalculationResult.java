package yowyob.comops.api.payroll.application.service;

import yowyob.comops.api.payroll.domain.model.PayElementCategory;

import java.math.BigDecimal;
import java.util.List;

/**
 * The full outcome of calculating one employee's pay: every computed line plus the rolled-up
 * headline figures. Pure value object; the orchestration layer turns it into a {@code PayrollEntry}
 * and its {@code PayslipLine}s.
 *
 * @param gross            total of EARNING lines
 * @param taxableNet       net imposable used for income tax (informational)
 * @param totalDeductions  total of DEDUCTION lines withheld from the employee
 * @param incomeTax        portion of deductions that is income tax (subset, for reporting)
 * @param employerCharges  total of EMPLOYER_CHARGE lines
 * @param voluntary        total of voluntary deductions (loans, advances, garnishments)
 * @param net              gross − statutory deductions − voluntary deductions
 * @param lines            every computed line in display order
 */
public record CalculationResult(
        BigDecimal gross,
        BigDecimal taxableNet,
        BigDecimal totalDeductions,
        BigDecimal incomeTax,
        BigDecimal employerCharges,
        BigDecimal voluntary,
        BigDecimal net,
        List<ComputedLine> lines) {

    public List<ComputedLine> linesOf(PayElementCategory category) {
        return lines.stream().filter(l -> l.category() == category).toList();
    }
}
