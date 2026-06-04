package yowyob.comops.api.payroll.application.service;

import java.math.BigDecimal;

/**
 * The assembled earnings that make up gross pay for one employee in one period.
 *
 * @param contractualBaseSalary the full contractual base (for reference / payslip header)
 * @param proratedBaseSalary    the base actually paid after proration (mid-month entry/exit)
 * @param benefitsInKind        valued benefits in kind
 * @param overtimeAmount        total overtime pay
 * @param bonuses               ad-hoc primes/bonuses for the period
 */
public record GrossComponents(
        BigDecimal contractualBaseSalary,
        BigDecimal proratedBaseSalary,
        BigDecimal benefitsInKind,
        BigDecimal overtimeAmount,
        BigDecimal bonuses) {

    public GrossComponents {
        contractualBaseSalary = nz(contractualBaseSalary);
        proratedBaseSalary = nz(proratedBaseSalary);
        benefitsInKind = nz(benefitsInKind);
        overtimeAmount = nz(overtimeAmount);
        bonuses = nz(bonuses);
    }

    /** Gross = prorated base + benefits in kind + overtime + bonuses, money-rounded. */
    public BigDecimal gross() {
        return Rounding.money(proratedBaseSalary
                .add(benefitsInKind)
                .add(overtimeAmount)
                .add(bonuses));
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
