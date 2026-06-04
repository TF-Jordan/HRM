package yowyob.comops.api.payroll.application.service;

import yowyob.comops.api.payroll.domain.model.LookupTable;
import yowyob.comops.api.payroll.domain.model.PayElement;
import yowyob.comops.api.payroll.domain.model.TaxBracketTable;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Evaluates a single {@link PayElement} against a {@link PayrollCalculationContext}, returning
 * the rounded amount. Pure and stateless.
 *
 * <p>An element is exempt (amount zero) when the run's gross is strictly below the element's
 * {@code exemptionThreshold} — this mirrors the Cameroonian rule where IRPP and CFC do not apply
 * below the 62,000 XAF monthly floor, regardless of the element's own base.
 */
public final class PayElementEvaluator {

    private PayElementEvaluator() {}

    public static BigDecimal evaluate(PayElement element, PayrollCalculationContext ctx,
                                      Map<String, TaxBracketTable> bracketTables,
                                      Map<String, LookupTable> lookupTables) {
        BigDecimal threshold = element.exemptionThreshold();
        if (threshold != null && ctx.resolve(PayrollCalculationContext.GROSS).compareTo(threshold) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal base = ctx.resolve(element.baseReference());
        return switch (element.method()) {
            case RATE -> {
                BigDecimal clamped = Rounding.clamp(base, element.floor(), element.ceiling());
                BigDecimal rate = element.rate() == null ? BigDecimal.ZERO : element.rate();
                yield Rounding.money(clamped.multiply(rate));
            }
            case FLAT -> Rounding.money(element.flatAmount());
            case BRACKET -> {
                TaxBracketTable table = bracketTables.get(element.bracketTableCode());
                if (table == null) {
                    throw new IllegalStateException(
                            "Missing tax bracket table '" + element.bracketTableCode()
                                    + "' for element '" + element.code() + "'");
                }
                yield table.apply(base);
            }
            case LOOKUP_TABLE -> {
                LookupTable table = lookupTables.get(element.lookupTableCode());
                if (table == null) {
                    throw new IllegalStateException(
                            "Missing lookup table '" + element.lookupTableCode()
                                    + "' for element '" + element.code() + "'");
                }
                yield Rounding.money(table.resolve(base));
            }
            case FORMULA -> throw new UnsupportedOperationException(
                    "FORMULA pay elements are not evaluated by the MVP engine (element '"
                            + element.code() + "')");
        };
    }
}
