package yowyob.comops.api.payroll.domain.model;

import java.math.BigDecimal;

/**
 * Aggregated, jurisdiction-agnostic totals of a payroll run.
 *
 * Unlike the legacy model (which hard-coded CNPS/IRPP columns), totals are grouped by the
 * generic {@link PayElementCategory} dimensions so the same shape fits any country.
 *
 * @param totalGross             sum of all EARNING amounts
 * @param totalEmployeeDeductions sum of all DEDUCTION amounts withheld from employees
 * @param totalIncomeTax         portion of deductions that is income tax (subset, for reporting)
 * @param totalNet               sum of net pay
 * @param totalEmployerCharges   sum of all EMPLOYER_CHARGE amounts
 * @param nbEmployes             number of employees included in the run
 */
public record PayrollRunTotals(
        BigDecimal totalGross,
        BigDecimal totalEmployeeDeductions,
        BigDecimal totalIncomeTax,
        BigDecimal totalNet,
        BigDecimal totalEmployerCharges,
        int nbEmployes) {

    public static PayrollRunTotals zero() {
        return new PayrollRunTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, 0);
    }
}
