package yowyob.comops.api.payroll.application.service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One employee's line in a statutory declaration. A service builds these by reading the
 * relevant payslip lines (e.g. the CNPS or IRPP amounts) for each employee in a run.
 *
 * @param employeeId           employee reference
 * @param matricule            payroll number
 * @param employeeName         display name
 * @param socialSecurityNo     CNPS number (or equivalent)
 * @param grossBase            contributory / taxable base
 * @param employeeContribution employee-side amount (e.g. CNPS salarié, or IRPP+CAC for tax)
 * @param employerContribution employer-side amount (e.g. CNPS patronal); zero for tax declarations
 */
public record DeclarationLineItem(
        UUID employeeId,
        String matricule,
        String employeeName,
        String socialSecurityNo,
        BigDecimal grossBase,
        BigDecimal employeeContribution,
        BigDecimal employerContribution) {
}
