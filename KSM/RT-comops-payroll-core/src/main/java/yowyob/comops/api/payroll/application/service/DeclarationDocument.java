package yowyob.comops.api.payroll.application.service;

import yowyob.comops.api.payroll.domain.model.DeclarationType;

import java.math.BigDecimal;
import java.util.List;

/**
 * An aggregated statutory declaration: its line items plus the rolled-up totals.
 *
 * @param type              declaration type
 * @param period            canonical {@code YYYY-MM} period
 * @param items             per-employee line items
 * @param totalGrossBase    sum of contributory/taxable bases
 * @param totalEmployee     sum of employee-side amounts
 * @param totalEmployer     sum of employer-side amounts
 * @param employeeCount     number of employees declared
 */
public record DeclarationDocument(
        DeclarationType type,
        String period,
        List<DeclarationLineItem> items,
        BigDecimal totalGrossBase,
        BigDecimal totalEmployee,
        BigDecimal totalEmployer,
        int employeeCount) {

    public BigDecimal grandTotal() {
        return totalEmployee.add(totalEmployer);
    }
}
