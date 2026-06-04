package yowyob.comops.api.payroll.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * A configurable payroll line definition — the heart of the country-agnostic engine.
 *
 * Replaces the hard-coded CNPS/IRPP/CAC/RAV/CFC/TDL constants of the legacy engine with
 * data that lives in the database, is scoped per tenant and country, and is versioned by
 * effective date. Adding a rule, changing a rate, or supporting a new jurisdiction becomes
 * a configuration change rather than a code change and redeploy.
 *
 * <p>How {@code amount} is derived depends on {@link #method}:
 * <ul>
 *   <li>{@code RATE} — {@code clamp(base(baseReference), floor, ceiling) * rate}</li>
 *   <li>{@code BRACKET} — progressive scale named by {@link #bracketTableCode}</li>
 *   <li>{@code FLAT} — {@link #flatAmount}</li>
 *   <li>{@code LOOKUP_TABLE} — stepped forfait named by {@link #lookupTableCode}</li>
 * </ul>
 *
 * <p>{@link #baseReference} names a slot resolved by the engine at calculation time,
 * e.g. {@code GROSS}, {@code BASE_SALARY}, {@code TAXABLE_NET}, {@code IRPP}.
 */
public final class PayElement extends BaseEntity {

    private final String code;
    private final String label;
    private final PayElementCategory category;
    private final CalculationMethod method;
    private final String baseReference;
    private final BigDecimal rate;
    private final BigDecimal ceiling;
    private final BigDecimal floor;
    private final BigDecimal exemptionThreshold;
    private final BigDecimal flatAmount;
    private final String bracketTableCode;
    private final String lookupTableCode;
    private final boolean taxable;
    private final boolean socialContributable;
    private final String countryCode;
    private final int displayOrder;
    private final boolean active;
    private final LocalDate effectiveFrom;
    private final LocalDate effectiveTo;

    private PayElement(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                       String code, String label, PayElementCategory category, CalculationMethod method,
                       String baseReference, BigDecimal rate, BigDecimal ceiling, BigDecimal floor,
                       BigDecimal exemptionThreshold, BigDecimal flatAmount, String bracketTableCode,
                       String lookupTableCode, boolean taxable, boolean socialContributable,
                       String countryCode, int displayOrder, boolean active,
                       LocalDate effectiveFrom, LocalDate effectiveTo) {
        super(id, tenantId, createdAt, updatedAt);
        this.code = Objects.requireNonNull(code, "code is required");
        this.label = Objects.requireNonNull(label, "label is required");
        this.category = Objects.requireNonNull(category, "category is required");
        this.method = Objects.requireNonNull(method, "method is required");
        this.baseReference = baseReference;
        this.rate = rate;
        this.ceiling = ceiling;
        this.floor = floor;
        this.exemptionThreshold = exemptionThreshold;
        this.flatAmount = flatAmount;
        this.bracketTableCode = bracketTableCode;
        this.lookupTableCode = lookupTableCode;
        this.taxable = taxable;
        this.socialContributable = socialContributable;
        this.countryCode = Objects.requireNonNull(countryCode, "countryCode is required");
        this.displayOrder = displayOrder;
        this.active = active;
        this.effectiveFrom = Objects.requireNonNull(effectiveFrom, "effectiveFrom is required");
        this.effectiveTo = effectiveTo;
    }

    public static PayElement create(UUID tenantId, String code, String label,
                                    PayElementCategory category, CalculationMethod method,
                                    String baseReference, BigDecimal rate, BigDecimal ceiling,
                                    BigDecimal floor, BigDecimal exemptionThreshold, BigDecimal flatAmount,
                                    String bracketTableCode, String lookupTableCode, boolean taxable,
                                    boolean socialContributable, String countryCode, int displayOrder,
                                    LocalDate effectiveFrom, LocalDate effectiveTo) {
        Instant now = Instant.now();
        return new PayElement(UUID.randomUUID(), tenantId, now, now, code, label, category, method,
                baseReference, rate, ceiling, floor, exemptionThreshold, flatAmount, bracketTableCode,
                lookupTableCode, taxable, socialContributable, countryCode, displayOrder, true,
                effectiveFrom, effectiveTo);
    }

    public static PayElement rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                       String code, String label, PayElementCategory category,
                                       CalculationMethod method, String baseReference, BigDecimal rate,
                                       BigDecimal ceiling, BigDecimal floor, BigDecimal exemptionThreshold,
                                       BigDecimal flatAmount, String bracketTableCode, String lookupTableCode,
                                       boolean taxable, boolean socialContributable, String countryCode,
                                       int displayOrder, boolean active, LocalDate effectiveFrom,
                                       LocalDate effectiveTo) {
        return new PayElement(id, tenantId, createdAt, updatedAt, code, label, category, method,
                baseReference, rate, ceiling, floor, exemptionThreshold, flatAmount, bracketTableCode,
                lookupTableCode, taxable, socialContributable, countryCode, displayOrder, active,
                effectiveFrom, effectiveTo);
    }

    /** Returns a deactivated copy (closes the element to future runs); bumps updatedAt. */
    public PayElement deactivate() {
        return new PayElement(id(), tenantId(), createdAt(), Instant.now(), code, label, category, method,
                baseReference, rate, ceiling, floor, exemptionThreshold, flatAmount, bracketTableCode,
                lookupTableCode, taxable, socialContributable, countryCode, displayOrder, false,
                effectiveFrom, effectiveTo);
    }

    public boolean isEffectiveOn(LocalDate date) {
        boolean started = !date.isBefore(effectiveFrom);
        boolean notEnded = effectiveTo == null || !date.isAfter(effectiveTo);
        return active && started && notEnded;
    }

    public PayslipLineType toPayslipLineType() {
        return switch (category) {
            case EARNING -> PayslipLineType.EARNING;
            case DEDUCTION -> PayslipLineType.DEDUCTION;
            case EMPLOYER_CHARGE, INFORMATIONAL -> PayslipLineType.EMPLOYER_INFO;
        };
    }

    public String code() { return code; }
    public String label() { return label; }
    public PayElementCategory category() { return category; }
    public CalculationMethod method() { return method; }
    public String baseReference() { return baseReference; }
    public BigDecimal rate() { return rate; }
    public BigDecimal ceiling() { return ceiling; }
    public BigDecimal floor() { return floor; }
    public BigDecimal exemptionThreshold() { return exemptionThreshold; }
    public BigDecimal flatAmount() { return flatAmount; }
    public String bracketTableCode() { return bracketTableCode; }
    public String lookupTableCode() { return lookupTableCode; }
    public boolean taxable() { return taxable; }
    public boolean socialContributable() { return socialContributable; }
    public String countryCode() { return countryCode; }
    public int displayOrder() { return displayOrder; }
    public boolean active() { return active; }
    public LocalDate effectiveFrom() { return effectiveFrom; }
    public LocalDate effectiveTo() { return effectiveTo; }
}
