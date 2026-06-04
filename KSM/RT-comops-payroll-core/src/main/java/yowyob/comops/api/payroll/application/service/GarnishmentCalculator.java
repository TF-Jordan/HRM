package yowyob.comops.api.payroll.application.service;

import yowyob.comops.api.payroll.domain.model.GarnishmentType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Computes how much of a net salary may be withheld for wage garnishments, and allocates it
 * across competing orders by legal priority. Pure and stateless.
 *
 * <p>The ordinary seizable quota is a progressive banded fraction of the net salary (the barème
 * is supplied by the caller, keeping the rule jurisdiction-configurable). Allocation rules:
 * <ul>
 *   <li>{@link GarnishmentType#ALIMONY} is served first and is bounded only by the net salary
 *       (it can reach the otherwise-protected fraction);</li>
 *   <li>{@link GarnishmentType#TAX_LEVY} then {@link GarnishmentType#CREDITOR} share the ordinary
 *       seizable quota, in that order;</li>
 *   <li>the total withheld never exceeds the net salary.</li>
 * </ul>
 */
public final class GarnishmentCalculator {

    private GarnishmentCalculator() {}

    /** A band of the seizable-quota scale: a fraction {@code rate} applies up to {@code upperBound}. */
    public record Band(BigDecimal upperBound, BigDecimal rate) {}

    /** A garnishment order claiming a monthly amount. */
    public record Request(GarnishmentType type, BigDecimal monthlyAmount) {}

    /** What was actually allocated to one order. */
    public record Allocation(GarnishmentType type, BigDecimal requested, BigDecimal allocated) {}

    /** Allocation outcome across all orders. */
    public record Result(BigDecimal ordinaryQuota, BigDecimal totalWithheld, List<Allocation> allocations) {}

    /**
     * Progressive seizable quota on {@code net} using {@code bands} (sorted by ascending bound;
     * the band with a null bound is the open top). Each band's rate applies to the slice of net
     * that falls within it.
     */
    public static BigDecimal ordinaryQuota(BigDecimal net, List<Band> bands) {
        if (net == null || net.signum() <= 0 || bands == null || bands.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<Band> sorted = new ArrayList<>(bands);
        sorted.sort(Comparator.comparing(b -> b.upperBound() == null
                ? BigDecimal.valueOf(Long.MAX_VALUE) : b.upperBound()));
        BigDecimal quota = BigDecimal.ZERO;
        BigDecimal lower = BigDecimal.ZERO;
        BigDecimal remaining = net;
        for (Band band : sorted) {
            if (remaining.signum() <= 0) break;
            BigDecimal sliceWidth = band.upperBound() == null
                    ? remaining
                    : band.upperBound().subtract(lower).min(remaining);
            if (sliceWidth.signum() > 0) {
                quota = quota.add(sliceWidth.multiply(band.rate()));
                remaining = remaining.subtract(sliceWidth);
                lower = band.upperBound();
            }
        }
        return Rounding.money(quota);
    }

    public static Result allocate(BigDecimal net, List<Request> requests, List<Band> bands) {
        BigDecimal netSafe = net == null ? BigDecimal.ZERO : net;
        BigDecimal quota = ordinaryQuota(netSafe, bands);

        List<Request> ordered = new ArrayList<>(requests);
        ordered.sort(Comparator.comparingInt(r -> r.type().ordinal()));

        List<Allocation> allocations = new ArrayList<>();
        BigDecimal withheld = BigDecimal.ZERO;
        BigDecimal quotaRemaining = quota;

        for (Request req : ordered) {
            BigDecimal requested = req.monthlyAmount() == null ? BigDecimal.ZERO : req.monthlyAmount();
            BigDecimal netRemaining = netSafe.subtract(withheld);
            BigDecimal allocated;
            if (req.type() == GarnishmentType.ALIMONY) {
                allocated = requested.min(netRemaining).max(BigDecimal.ZERO);
            } else {
                allocated = requested.min(quotaRemaining).min(netRemaining).max(BigDecimal.ZERO);
                quotaRemaining = quotaRemaining.subtract(allocated);
            }
            allocations.add(new Allocation(req.type(), Rounding.money(requested), Rounding.money(allocated)));
            withheld = withheld.add(allocated);
        }
        return new Result(quota, Rounding.money(withheld), List.copyOf(allocations));
    }
}
