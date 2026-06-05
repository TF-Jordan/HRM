package yowyob.comops.api.payroll.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A versioned, country-scoped progressive scale (e.g. {@code IRPP_CM_2026}).
 *
 * Holds its ordered {@link TaxBracket}s and knows how to apply itself to a taxable base.
 * The raw progressive math lives here (intrinsic behaviour); higher-level concerns such
 * as abatements and the family quotient are orchestrated by the calculation engine.
 */
public final class TaxBracketTable extends BaseEntity {

    private final String code;
    private final String label;
    private final String countryCode;
    private final LocalDate effectiveFrom;
    private final LocalDate effectiveTo;
    private final boolean active;
    private final List<TaxBracket> brackets;

    private TaxBracketTable(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                            String code, String label, String countryCode,
                            LocalDate effectiveFrom, LocalDate effectiveTo, boolean active,
                            List<TaxBracket> brackets) {
        super(id, tenantId, createdAt, updatedAt);
        this.code = Objects.requireNonNull(code, "code is required");
        this.label = Objects.requireNonNull(label, "label is required");
        this.countryCode = Objects.requireNonNull(countryCode, "countryCode is required");
        this.effectiveFrom = Objects.requireNonNull(effectiveFrom, "effectiveFrom is required");
        this.effectiveTo = effectiveTo;
        this.active = active;
        List<TaxBracket> sorted = new ArrayList<>(Objects.requireNonNull(brackets, "brackets are required"));
        sorted.sort(Comparator.comparingInt(TaxBracket::ordre));
        this.brackets = List.copyOf(sorted);
    }

    public static TaxBracketTable create(UUID tenantId, String code, String label, String countryCode,
                                         LocalDate effectiveFrom, LocalDate effectiveTo,
                                         List<TaxBracket> brackets) {
        Instant now = Instant.now();
        return new TaxBracketTable(UUID.randomUUID(), tenantId, now, now, code, label, countryCode,
                effectiveFrom, effectiveTo, true, brackets);
    }

    public static TaxBracketTable rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                            String code, String label, String countryCode,
                                            LocalDate effectiveFrom, LocalDate effectiveTo, boolean active,
                                            List<TaxBracket> brackets) {
        return new TaxBracketTable(id, tenantId, createdAt, updatedAt, code, label, countryCode,
                effectiveFrom, effectiveTo, active, brackets);
    }

    /**
     * Applies the progressive scale to a (non-negative) taxable base, summing the marginal
     * contributions of each bracket. The result is rounded to the nearest unit (XAF has no
     * minor unit) using HALF_UP, consistent with Cameroonian payroll practice.
     */
    public BigDecimal apply(BigDecimal taxableBase) {
        if (taxableBase == null || taxableBase.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal remaining = taxableBase;
        for (TaxBracket bracket : brackets) {
            if (remaining.signum() <= 0) {
                break;
            }
            BigDecimal slice = bracket.isOpenTop() ? remaining : remaining.min(bracket.width());
            tax = tax.add(slice.multiply(bracket.rate()));
            remaining = remaining.subtract(slice);
        }
        return tax.setScale(0, RoundingMode.HALF_UP);
    }

    public boolean isEffectiveOn(LocalDate date) {
        boolean started = !date.isBefore(effectiveFrom);
        boolean notEnded = effectiveTo == null || !date.isAfter(effectiveTo);
        return active && started && notEnded;
    }

    /** Returns a deactivated copy (closes the scale to future runs); bumps updatedAt. */
    public TaxBracketTable deactivate() {
        return new TaxBracketTable(id(), tenantId(), createdAt(), Instant.now(), code, label,
                countryCode, effectiveFrom, effectiveTo, false, brackets);
    }

    public String code() { return code; }
    public String label() { return label; }
    public String countryCode() { return countryCode; }
    public LocalDate effectiveFrom() { return effectiveFrom; }
    public LocalDate effectiveTo() { return effectiveTo; }
    public boolean active() { return active; }
    public List<TaxBracket> brackets() { return brackets; }
}
