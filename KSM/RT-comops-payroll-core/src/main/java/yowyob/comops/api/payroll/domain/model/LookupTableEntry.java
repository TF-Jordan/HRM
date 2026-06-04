package yowyob.comops.api.payroll.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A stepped row of a {@link LookupTable}: when the lookup base falls within
 * {@code [lowerBound, upperBound]}, the flat {@code amount} applies.
 *
 * @param ordre       evaluation order (ascending bounds)
 * @param lowerBound  inclusive lower bound on the lookup base
 * @param upperBound  inclusive upper bound, or {@code null} for the open top step
 * @param amount      flat forfait amount for this step
 */
public record LookupTableEntry(int ordre, BigDecimal lowerBound, BigDecimal upperBound, BigDecimal amount) {

    public LookupTableEntry {
        Objects.requireNonNull(lowerBound, "lowerBound is required");
        Objects.requireNonNull(amount, "amount is required");
        if (upperBound != null && upperBound.compareTo(lowerBound) < 0) {
            throw new IllegalArgumentException(
                    "upperBound must be >= lowerBound (step " + ordre + ")");
        }
    }

    public boolean contains(BigDecimal base) {
        boolean aboveLower = base.compareTo(lowerBound) >= 0;
        boolean belowUpper = upperBound == null || base.compareTo(upperBound) <= 0;
        return aboveLower && belowUpper;
    }
}
