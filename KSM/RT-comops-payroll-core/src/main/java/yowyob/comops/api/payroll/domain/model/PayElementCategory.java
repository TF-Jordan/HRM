package yowyob.comops.api.payroll.domain.model;

/**
 * Nature of a pay element (rubrique de paie).
 *
 * <ul>
 *   <li>{@link #EARNING} — adds to gross pay (salaire de base, primes, heures sup…)</li>
 *   <li>{@link #DEDUCTION} — withheld from the employee (CNPS salarié, IRPP, CFC…)</li>
 *   <li>{@link #EMPLOYER_CHARGE} — borne by the employer, informational on the payslip
 *       (CNPS patronal, FNE, taxe patronale…)</li>
 *   <li>{@link #INFORMATIONAL} — neither earning nor deduction, displayed for reference
 *       (net imposable, base CNPS…)</li>
 * </ul>
 */
public enum PayElementCategory {
    EARNING,
    DEDUCTION,
    EMPLOYER_CHARGE,
    INFORMATIONAL
}
