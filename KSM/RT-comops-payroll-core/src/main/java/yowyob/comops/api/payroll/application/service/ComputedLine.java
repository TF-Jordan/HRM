package yowyob.comops.api.payroll.application.service;

import yowyob.comops.api.payroll.domain.model.PayElementCategory;

import java.math.BigDecimal;

/**
 * The result of evaluating one pay element: enough to render a payslip line and to roll up
 * the run totals. Pure value object produced by the calculation engine (no persistence).
 *
 * @param code     pay-element code ({@code null} for synthetic lines like the base salary)
 * @param label    human-readable label
 * @param category nature of the line (drives whether it adds to gross, is withheld, etc.)
 * @param base     calculation base actually used ({@code null} when not applicable)
 * @param rate     rate actually applied ({@code null} for flat/lookup/bracket lines)
 * @param amount   resulting amount (rounded to the currency's whole unit)
 */
public record ComputedLine(
        String code,
        String label,
        PayElementCategory category,
        BigDecimal base,
        BigDecimal rate,
        BigDecimal amount) {

    public static ComputedLine of(String code, String label, PayElementCategory category,
                                  BigDecimal base, BigDecimal rate, BigDecimal amount) {
        return new ComputedLine(code, label, category, base, rate, Rounding.money(amount));
    }
}
