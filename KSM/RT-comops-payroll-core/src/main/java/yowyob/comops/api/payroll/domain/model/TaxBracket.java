package yowyob.comops.api.payroll.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A single progressive bracket within a {@link TaxBracketTable}.
 *
 * @param ordre       display / evaluation order (1-based, ascending bounds)
 * @param lowerBound  inclusive lower bound of the bracket (on the taxable base)
 * @param upperBound  exclusive-style upper bound, or {@code null} for the open top bracket
 * @param rate        marginal rate applied to the portion of the base inside this bracket
 */
public record TaxBracket(int ordre, BigDecimal lowerBound, BigDecimal upperBound, BigDecimal rate) {

    public TaxBracket {
        Objects.requireNonNull(lowerBound, "lowerBound is required");
        Objects.requireNonNull(rate, "rate is required");
        if (upperBound != null && upperBound.compareTo(lowerBound) <= 0) {
            throw new IllegalArgumentException(
                    "upperBound must be greater than lowerBound (bracket " + ordre + ")");
        }
    }

    public boolean isOpenTop() {
        return upperBound == null;
    }

    /** Width of the bracket, or {@code null} for the open top bracket. */
    public BigDecimal width() {
        return upperBound == null ? null : upperBound.subtract(lowerBound);
    }
}
