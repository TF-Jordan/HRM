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
    private final int nbEmployes;
    private final Instant calculatedAt;
    private final UUID validatedBy;
    private final Instant validatedAt;

    private PayrollRun(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                       UUID organizationId, UUID agencyId, String periode, PayrollRunStatus status,
                       BigDecimal totalBrut, BigDecimal totalNet, BigDecimal totalCnpsEmploye,
                       BigDecimal totalCnpsEmployeur, BigDecimal totalIrpp, int nbEmployes,
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
        this.nbEmployes = nbEmployes;
        this.calculatedAt = calculatedAt;
        this.validatedBy = validatedBy;
        this.validatedAt = validatedAt;
    }

    public static PayrollRun create(UUID tenantId, UUID organizationId, UUID agencyId, String periode,
                                     BigDecimal totalBrut, BigDecimal totalNet,
                                     BigDecimal totalCnpsEmploye, BigDecimal totalCnpsEmployeur,
                                     BigDecimal totalIrpp, int nbEmployes) {
        return create(UUID.randomUUID(), tenantId, organizationId, agencyId, periode, totalBrut,
                totalNet, totalCnpsEmploye, totalCnpsEmployeur, totalIrpp, nbEmployes);
    }

    public static PayrollRun create(UUID id, UUID tenantId, UUID organizationId, UUID agencyId,
                                     String periode, BigDecimal totalBrut, BigDecimal totalNet,
                                     BigDecimal totalCnpsEmploye, BigDecimal totalCnpsEmployeur,
                                     BigDecimal totalIrpp, int nbEmployes) {
        Instant now = Instant.now();
        return new PayrollRun(id, tenantId, now, now, organizationId, agencyId, periode,
                PayrollRunStatus.CALCULATED, totalBrut, totalNet, totalCnpsEmploye, totalCnpsEmployeur,
                totalIrpp, nbEmployes, now, null, null);
    }

    public static PayrollRun rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                        UUID organizationId, UUID agencyId, String periode,
                                        PayrollRunStatus status, BigDecimal totalBrut, BigDecimal totalNet,
                                        BigDecimal totalCnpsEmploye, BigDecimal totalCnpsEmployeur,
                                        BigDecimal totalIrpp, int nbEmployes, Instant calculatedAt,
                                        UUID validatedBy, Instant validatedAt) {
        return new PayrollRun(id, tenantId, createdAt, updatedAt, organizationId, agencyId, periode,
                status, totalBrut, totalNet, totalCnpsEmploye, totalCnpsEmployeur, totalIrpp,
                nbEmployes, calculatedAt, validatedBy, validatedAt);
    }

    public PayrollRun withTotals(BigDecimal totalBrut, BigDecimal totalNet,
                                   BigDecimal totalCnpsEmploye, BigDecimal totalCnpsEmployeur,
                                   BigDecimal totalIrpp, int nbEmployes) {
        return new PayrollRun(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                periode, status, totalBrut, totalNet, totalCnpsEmploye, totalCnpsEmployeur, totalIrpp,
                nbEmployes, calculatedAt, validatedBy, validatedAt);
    }

    public PayrollRun validate(UUID validatorId) {
        if (this.status != PayrollRunStatus.CALCULATED) {
            throw new IllegalStateException("Cannot validate payroll run in status " + this.status);
        }
        Instant now = Instant.now();
        return new PayrollRun(id(), tenantId(), createdAt(), now, organizationId, agencyId, periode,
                PayrollRunStatus.VALIDATED, totalBrut, totalNet, totalCnpsEmploye, totalCnpsEmployeur,
                totalIrpp, nbEmployes, calculatedAt, validatorId, now);
    }

    public PayrollRun markPaid() {
        if (this.status != PayrollRunStatus.VALIDATED) {
            throw new IllegalStateException("Cannot mark as paid payroll run in status " + this.status);
        }
        return new PayrollRun(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                periode, PayrollRunStatus.PAID, totalBrut, totalNet, totalCnpsEmploye,
                totalCnpsEmployeur, totalIrpp, nbEmployes, calculatedAt, validatedBy, validatedAt);
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
    public int nbEmployes() { return nbEmployes; }
    public Instant calculatedAt() { return calculatedAt; }
    public UUID validatedBy() { return validatedBy; }
    public Instant validatedAt() { return validatedAt; }
}
