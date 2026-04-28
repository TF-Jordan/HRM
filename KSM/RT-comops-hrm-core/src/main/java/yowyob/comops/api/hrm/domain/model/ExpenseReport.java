package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ExpenseReport extends BaseEntity {

    private final UUID employeeId;
    private final String periode;
    private final BigDecimal totalMontant;
    private final String motif;
    private final ExpenseReportStatus status;

    private ExpenseReport(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                          UUID employeeId, String periode, BigDecimal totalMontant,
                          String motif, ExpenseReportStatus status) {
        super(id, tenantId, createdAt, updatedAt);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.periode = Objects.requireNonNull(periode);
        this.status = Objects.requireNonNull(status);
        this.totalMontant = totalMontant;
        this.motif = motif;
    }

    public static ExpenseReport create(UUID tenantId, UUID employeeId, String periode, String motif) {
        Instant now = Instant.now();
        return new ExpenseReport(UUID.randomUUID(), tenantId, now, now, employeeId, periode,
                BigDecimal.ZERO, motif, ExpenseReportStatus.DRAFT);
    }

    public static ExpenseReport rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                           UUID employeeId, String periode, BigDecimal totalMontant,
                                           String motif, ExpenseReportStatus status) {
        return new ExpenseReport(id, tenantId, createdAt, updatedAt, employeeId, periode,
                totalMontant, motif, status);
    }

    public ExpenseReport submit() {
        if (this.status != ExpenseReportStatus.DRAFT) throw new IllegalStateException("Cannot submit expense report in status " + this.status);
        return new ExpenseReport(id(), tenantId(), createdAt(), Instant.now(), employeeId, periode,
                totalMontant, motif, ExpenseReportStatus.SUBMITTED);
    }

    public ExpenseReport approve() {
        if (this.status != ExpenseReportStatus.SUBMITTED) throw new IllegalStateException("Cannot approve expense report in status " + this.status);
        return new ExpenseReport(id(), tenantId(), createdAt(), Instant.now(), employeeId, periode,
                totalMontant, motif, ExpenseReportStatus.APPROVED);
    }

    public ExpenseReport reject() {
        if (this.status != ExpenseReportStatus.SUBMITTED) throw new IllegalStateException("Cannot reject expense report in status " + this.status);
        return new ExpenseReport(id(), tenantId(), createdAt(), Instant.now(), employeeId, periode,
                totalMontant, motif, ExpenseReportStatus.REJECTED);
    }

    public ExpenseReport reimburse() {
        if (this.status != ExpenseReportStatus.APPROVED) throw new IllegalStateException("Cannot reimburse expense report in status " + this.status);
        return new ExpenseReport(id(), tenantId(), createdAt(), Instant.now(), employeeId, periode,
                totalMontant, motif, ExpenseReportStatus.REIMBURSED);
    }

    public ExpenseReport withTotalMontant(BigDecimal totalMontant) {
        return new ExpenseReport(id(), tenantId(), createdAt(), Instant.now(), employeeId, periode,
                totalMontant, motif, status);
    }

    public UUID employeeId() { return employeeId; }
    public String periode() { return periode; }
    public BigDecimal totalMontant() { return totalMontant; }
    public String motif() { return motif; }
    public ExpenseReportStatus status() { return status; }
}
