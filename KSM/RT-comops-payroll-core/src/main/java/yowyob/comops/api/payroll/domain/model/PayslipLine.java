package yowyob.comops.api.payroll.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One detailed line of a payslip, traceable back to the {@link PayElement} that produced it
 * (via {@link #payElementCode}; null for synthetic lines such as the base-salary header).
 */
public final class PayslipLine extends BaseEntity {

    private final UUID payrollEntryId;
    private final String payElementCode;
    private final String libelle;
    private final PayslipLineType type;
    private final BigDecimal base;
    private final BigDecimal taux;
    private final BigDecimal montant;
    private final int ordreAffichage;

    private PayslipLine(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                        UUID payrollEntryId, String payElementCode, String libelle, PayslipLineType type,
                        BigDecimal base, BigDecimal taux, BigDecimal montant, int ordreAffichage) {
        super(id, tenantId, createdAt, updatedAt);
        this.payrollEntryId = Objects.requireNonNull(payrollEntryId, "payrollEntryId is required");
        this.libelle = Objects.requireNonNull(libelle, "libelle is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.montant = Objects.requireNonNull(montant, "montant is required");
        this.payElementCode = payElementCode;
        this.base = base;
        this.taux = taux;
        this.ordreAffichage = ordreAffichage;
    }

    public static PayslipLine create(UUID tenantId, UUID payrollEntryId, String payElementCode,
                                     String libelle, PayslipLineType type, BigDecimal base,
                                     BigDecimal taux, BigDecimal montant, int ordreAffichage) {
        Instant now = Instant.now();
        return new PayslipLine(UUID.randomUUID(), tenantId, now, now, payrollEntryId, payElementCode,
                libelle, type, base, taux, montant, ordreAffichage);
    }

    public static PayslipLine rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                        UUID payrollEntryId, String payElementCode, String libelle,
                                        PayslipLineType type, BigDecimal base, BigDecimal taux,
                                        BigDecimal montant, int ordreAffichage) {
        return new PayslipLine(id, tenantId, createdAt, updatedAt, payrollEntryId, payElementCode,
                libelle, type, base, taux, montant, ordreAffichage);
    }

    public UUID payrollEntryId() { return payrollEntryId; }
    public String payElementCode() { return payElementCode; }
    public String libelle() { return libelle; }
    public PayslipLineType type() { return type; }
    public BigDecimal base() { return base; }
    public BigDecimal taux() { return taux; }
    public BigDecimal montant() { return montant; }
    public int ordreAffichage() { return ordreAffichage; }
}
