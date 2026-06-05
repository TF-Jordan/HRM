package yowyob.comops.api.payroll.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A versioned, country-scoped stepped forfait scale (e.g. {@code RAV_CM}, {@code TDL_CM}).
 *
 * Resolves a flat amount from the step whose range contains the lookup base. Returns
 * {@link BigDecimal#ZERO} when no step matches (e.g. below the first threshold).
 */
public final class LookupTable extends BaseEntity {

    private final String code;
    private final String label;
    private final String countryCode;
    private final LocalDate effectiveFrom;
    private final LocalDate effectiveTo;
    private final boolean active;
    private final List<LookupTableEntry> entries;

    private LookupTable(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                        String code, String label, String countryCode,
                        LocalDate effectiveFrom, LocalDate effectiveTo, boolean active,
                        List<LookupTableEntry> entries) {
        super(id, tenantId, createdAt, updatedAt);
        this.code = Objects.requireNonNull(code, "code is required");
        this.label = Objects.requireNonNull(label, "label is required");
        this.countryCode = Objects.requireNonNull(countryCode, "countryCode is required");
        this.effectiveFrom = Objects.requireNonNull(effectiveFrom, "effectiveFrom is required");
        this.effectiveTo = effectiveTo;
        this.active = active;
        List<LookupTableEntry> sorted = new ArrayList<>(Objects.requireNonNull(entries, "entries are required"));
        sorted.sort(Comparator.comparingInt(LookupTableEntry::ordre));
        this.entries = List.copyOf(sorted);
    }

    public static LookupTable create(UUID tenantId, String code, String label, String countryCode,
                                     LocalDate effectiveFrom, LocalDate effectiveTo,
                                     List<LookupTableEntry> entries) {
        Instant now = Instant.now();
        return new LookupTable(UUID.randomUUID(), tenantId, now, now, code, label, countryCode,
                effectiveFrom, effectiveTo, true, entries);
    }

    public static LookupTable rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                        String code, String label, String countryCode,
                                        LocalDate effectiveFrom, LocalDate effectiveTo, boolean active,
                                        List<LookupTableEntry> entries) {
        return new LookupTable(id, tenantId, createdAt, updatedAt, code, label, countryCode,
                effectiveFrom, effectiveTo, active, entries);
    }

    /** Resolves the forfait for a lookup base, or {@link BigDecimal#ZERO} if no step matches. */
    public BigDecimal resolve(BigDecimal base) {
        if (base == null) {
            return BigDecimal.ZERO;
        }
        for (LookupTableEntry entry : entries) {
            if (entry.contains(base)) {
                return entry.amount();
            }
        }
        return BigDecimal.ZERO;
    }

    public boolean isEffectiveOn(LocalDate date) {
        boolean started = !date.isBefore(effectiveFrom);
        boolean notEnded = effectiveTo == null || !date.isAfter(effectiveTo);
        return active && started && notEnded;
    }

    /** Returns a deactivated copy (closes the scale to future runs); bumps updatedAt. */
    public LookupTable deactivate() {
        return new LookupTable(id(), tenantId(), createdAt(), Instant.now(), code, label,
                countryCode, effectiveFrom, effectiveTo, false, entries);
    }

    public String code() { return code; }
    public String label() { return label; }
    public String countryCode() { return countryCode; }
    public LocalDate effectiveFrom() { return effectiveFrom; }
    public LocalDate effectiveTo() { return effectiveTo; }
    public boolean active() { return active; }
    public List<LookupTableEntry> entries() { return entries; }
}
