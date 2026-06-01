package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PayrollRun extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final String periode;
    private final PayrollRunStatus status;
    private final BigDecimal totalBrut;
    private final BigDecimal totalNet;
    private final BigDecimal totalCnpsEmploye;
    private final BigDecimal totalCnpsEmployeur;
    private final BigDecimal totalIrpp;
    private final BigDecimal totalCac;
    private final BigDecimal totalCfc;
    private final int nbEmployes;
    private final Instant calculatedAt;
    private final UUID validatedBy;
    private final Instant validatedAt;

    private PayrollRun(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                       UUID organizationId, UUID agencyId, String periode, PayrollRunStatus status,
                       BigDecimal totalBrut, BigDecimal totalNet, BigDecimal totalCnpsEmploye,
                       BigDecimal totalCnpsEmployeur, BigDecimal totalIrpp, BigDecimal totalCac,
                       BigDecimal totalCfc, int nbEmployes,
                       Instant calculatedAt, UUID validatedBy, Instant validatedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.periode = Objects.requireNonNull(periode);
        this.status = Objects.requireNonNull(status);
        this.agencyId = agencyId;
        this.totalBrut = totalBrut;
        this.totalNet = totalNet;
        this.totalCnpsEmploye = totalCnpsEmploye;
        this.totalCnpsEmployeur = totalCnpsEmployeur;
        this.totalIrpp = totalIrpp;
        this.totalCac = totalCac;
        this.totalCfc = totalCfc;
        this.nbEmployes = nbEmployes;
        this.calculatedAt = calculatedAt;
        this.validatedBy = validatedBy;
        this.validatedAt = validatedAt;
    }

    public static PayrollRun create(UUID tenantId, UUID organizationId, UUID agencyId, String periode,
                                     BigDecimal totalBrut, BigDecimal totalNet,
                                     BigDecimal totalCnpsEmploye, BigDecimal totalCnpsEmployeur,
                                     BigDecimal totalIrpp, BigDecimal totalCac, BigDecimal totalCfc,
                                     int nbEmployes) {
        Instant now = Instant.now();
        return new PayrollRun(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, periode,
                PayrollRunStatus.CALCULATED, totalBrut, totalNet, totalCnpsEmploye, totalCnpsEmployeur,
                totalIrpp, totalCac, totalCfc, nbEmployes, now, null, null);
    }

    /** Creates a placeholder run with a pre-determined id and zero totals, so it can be
     *  persisted before entries (satisfying the FK constraint) and updated afterwards. */
    public static PayrollRun createPlaceholder(UUID id, UUID tenantId, UUID organizationId,
                                                UUID agencyId, String periode) {
        Instant now = Instant.now();
        return new PayrollRun(id, tenantId, now, now, organizationId, agencyId, periode,
                PayrollRunStatus.CALCULATED, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, now, null, null);
    }

    /** Returns a new instance with the final aggregated totals. updatedAt is bumped so that
     *  isNew() returns false and Spring Data issues an UPDATE instead of INSERT. */
    public PayrollRun withFinalTotals(BigDecimal totalBrut, BigDecimal totalNet,
                                       BigDecimal totalCnpsEmploye, BigDecimal totalCnpsEmployeur,
                                       BigDecimal totalIrpp, BigDecimal totalCac, BigDecimal totalCfc,
                                       int nbEmployes) {
        return new PayrollRun(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                periode, status, totalBrut, totalNet, totalCnpsEmploye, totalCnpsEmployeur,
                totalIrpp, totalCac, totalCfc, nbEmployes, calculatedAt, validatedBy, validatedAt);
    }

    public static PayrollRun rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                        UUID organizationId, UUID agencyId, String periode,
                                        PayrollRunStatus status, BigDecimal totalBrut, BigDecimal totalNet,
                                        BigDecimal totalCnpsEmploye, BigDecimal totalCnpsEmployeur,
                                        BigDecimal totalIrpp, BigDecimal totalCac, BigDecimal totalCfc,
                                        int nbEmployes, Instant calculatedAt,
                                        UUID validatedBy, Instant validatedAt) {
        return new PayrollRun(id, tenantId, createdAt, updatedAt, organizationId, agencyId, periode,
                status, totalBrut, totalNet, totalCnpsEmploye, totalCnpsEmployeur, totalIrpp,
                totalCac, totalCfc, nbEmployes, calculatedAt, validatedBy, validatedAt);
    }

    public PayrollRun validate(UUID validatorId) {
        if (this.status != PayrollRunStatus.CALCULATED) {
            throw new IllegalStateException("Cannot validate payroll run in status " + this.status);
        }
        Instant now = Instant.now();
        return new PayrollRun(id(), tenantId(), createdAt(), now, organizationId, agencyId, periode,
                PayrollRunStatus.VALIDATED, totalBrut, totalNet, totalCnpsEmploye, totalCnpsEmployeur,
                totalIrpp, totalCac, totalCfc, nbEmployes, calculatedAt, validatorId, now);
    }

    public PayrollRun markPaid() {
        if (this.status != PayrollRunStatus.VALIDATED) {
            throw new IllegalStateException("Cannot mark as paid payroll run in status " + this.status);
        }
        return new PayrollRun(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                periode, PayrollRunStatus.PAID, totalBrut, totalNet, totalCnpsEmploye,
                totalCnpsEmployeur, totalIrpp, totalCac, totalCfc, nbEmployes,
                calculatedAt, validatedBy, validatedAt);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public String periode() { return periode; }
    public PayrollRunStatus status() { return status; }
    public BigDecimal totalBrut() { return totalBrut; }
    public BigDecimal totalNet() { return totalNet; }
    public BigDecimal totalCnpsEmploye() { return totalCnpsEmploye; }
    public BigDecimal totalCnpsEmployeur() { return totalCnpsEmployeur; }
    public BigDecimal totalIrpp() { return totalIrpp; }
    public BigDecimal totalCac() { return totalCac; }
    public BigDecimal totalCfc() { return totalCfc; }
    public int nbEmployes() { return nbEmployes; }
    public Instant calculatedAt() { return calculatedAt; }
    public UUID validatedBy() { return validatedBy; }
    public Instant validatedAt() { return validatedAt; }
}
