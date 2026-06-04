package yowyob.comops.api.payroll.application.port.in;

import yowyob.comops.api.payroll.domain.model.CalculationMethod;
import yowyob.comops.api.payroll.domain.model.PayElementCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command to define a new {@link yowyob.comops.api.payroll.domain.model.PayElement}.
 * The tenant is taken from the request context, not the command.
 */
public record CreatePayElementCommand(
        String code,
        String label,
        PayElementCategory category,
        CalculationMethod method,
        String baseReference,
        BigDecimal rate,
        BigDecimal ceiling,
        BigDecimal floor,
        BigDecimal exemptionThreshold,
        BigDecimal flatAmount,
        String bracketTableCode,
        String lookupTableCode,
        boolean taxable,
        boolean socialContributable,
        String countryCode,
        int displayOrder,
        LocalDate effectiveFrom,
        LocalDate effectiveTo) {
}
