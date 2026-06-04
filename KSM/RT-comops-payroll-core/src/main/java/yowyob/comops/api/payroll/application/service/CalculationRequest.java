package yowyob.comops.api.payroll.application.service;

import yowyob.comops.api.payroll.domain.model.LookupTable;
import yowyob.comops.api.payroll.domain.model.PayElement;
import yowyob.comops.api.payroll.domain.model.TaxBracketTable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Immutable input to {@link PayrollCalculationEngine#calculate}. Bundles the assembled gross,
 * the taxable-net policy, the ordered active pay elements, the reference tables, the precomputed
 * voluntary deductions, and the set of codes counted as income tax (for reporting).
 *
 * @param gross               assembled earnings
 * @param abatementRate       professional-expense abatement rate applied to gross (e.g. 0.30)
 * @param abatementCap        cap on the abatement amount (e.g. 400000); null = uncapped
 * @param elements            active deduction/charge/informational elements (any order)
 * @param bracketTables       progressive scales keyed by code
 * @param lookupTables        stepped forfait scales keyed by code
 * @param voluntaryDeductions total voluntary deductions withheld from net (loans, advances, garnishments)
 * @param incomeTaxCodes      element codes whose amounts are reported as income tax
 */
public record CalculationRequest(
        GrossComponents gross,
        BigDecimal abatementRate,
        BigDecimal abatementCap,
        List<PayElement> elements,
        Map<String, TaxBracketTable> bracketTables,
        Map<String, LookupTable> lookupTables,
        BigDecimal voluntaryDeductions,
        Set<String> incomeTaxCodes) {
}
