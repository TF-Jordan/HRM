package yowyob.comops.api.payroll.domain.model;

/**
 * How a pay element's amount is derived from its base.
 *
 * <ul>
 *   <li>{@link #RATE} — {@code amount = clamp(base, floor, ceiling) * rate}</li>
 *   <li>{@link #BRACKET} — progressive computation over a {@code TaxBracketTable}</li>
 *   <li>{@link #FLAT} — fixed {@code flatAmount}, independent of any base</li>
 *   <li>{@link #LOOKUP_TABLE} — stepped forfait resolved from a {@code LookupTable}
 *       (used for RAV, TDL…)</li>
 *   <li>{@link #FORMULA} — reserved for future scripted rules; not evaluated by the MVP engine</li>
 * </ul>
 */
public enum CalculationMethod {
    RATE,
    BRACKET,
    FLAT,
    LOOKUP_TABLE,
    FORMULA
}
