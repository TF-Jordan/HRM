package yowyob.comops.api.payroll.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A payroll cycle for a (period, organization, agency), with a ten-state lifecycle and
 * a segregation-of-duties split between validation (HR admin) and approval (finance).
 *
 * <pre>
 * DRAFT → VARIABLES_LOCKED → CALCULATED → REVIEW → VALIDATED → APPROVED → PAYMENT_INITIATED → PAID → CLOSED
 *                                  ↑          │
 *                                  └─ REJECTED ┘
 * </pre>
 *
 * Immutable: every transition returns a new instance with {@code updatedAt} bumped, so that
 * {@code isNew()} on the persistence entity correctly distinguishes INSERT from UPDATE.
 */
public final class PayrollRun extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final PayPeriod period;
    private final RunType runType;
    private final PayrollRunStatus status;
    private final String currency;

    private final BigDecimal totalGross;
    private final BigDecimal totalEmployeeDeductions;
    private final BigDecimal totalIncomeTax;
    private final BigDecimal totalNet;
    private final BigDecimal totalEmployerCharges;
    private final int nbEmployes;

    private final Instant calculatedAt;
    private final UUID validatedBy;
    private final Instant validatedAt;
    private final UUID approvedBy;
    private final Instant approvedAt;
    private final Instant paidAt;
    private final Instant closedAt;

    // Rejection ("mise en révision" by the HR admin): mandatory justification routed back to
    // the payroll manager. Preserved as an audit trail even after the cycle is recalculated.
    private final String rejectionReason;
    private final UUID rejectedBy;
    private final Instant rejectedAt;

    private PayrollRun(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                       UUID organizationId, UUID agencyId, PayPeriod period, RunType runType,
                       PayrollRunStatus status, String currency,
                       BigDecimal totalGross, BigDecimal totalEmployeeDeductions, BigDecimal totalIncomeTax,
                       BigDecimal totalNet, BigDecimal totalEmployerCharges, int nbEmployes,
                       Instant calculatedAt, UUID validatedBy, Instant validatedAt,
                       UUID approvedBy, Instant approvedAt, Instant paidAt, Instant closedAt,
                       String rejectionReason, UUID rejectedBy, Instant rejectedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.period = Objects.requireNonNull(period, "period is required");
        this.runType = Objects.requireNonNull(runType, "runType is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.currency = Objects.requireNonNull(currency, "currency is required");
        this.agencyId = agencyId;
        this.totalGross = totalGross;
        this.totalEmployeeDeductions = totalEmployeeDeductions;
        this.totalIncomeTax = totalIncomeTax;
        this.totalNet = totalNet;
        this.totalEmployerCharges = totalEmployerCharges;
        this.nbEmployes = nbEmployes;
        this.calculatedAt = calculatedAt;
        this.validatedBy = validatedBy;
        this.validatedAt = validatedAt;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.paidAt = paidAt;
        this.closedAt = closedAt;
        this.rejectionReason = rejectionReason;
        this.rejectedBy = rejectedBy;
        this.rejectedAt = rejectedAt;
    }

    /** Opens a fresh run in {@link PayrollRunStatus#DRAFT} with zeroed totals. */
    public static PayrollRun open(UUID tenantId, UUID organizationId, UUID agencyId,
                                  PayPeriod period, RunType runType, String currency) {
        Instant now = Instant.now();
        return new PayrollRun(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, period,
                runType, PayrollRunStatus.DRAFT, currency,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0,
                null, null, null, null, null, null, null,
                null, null, null);
    }

    public static PayrollRun rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                       UUID organizationId, UUID agencyId, PayPeriod period, RunType runType,
                                       PayrollRunStatus status, String currency,
                                       BigDecimal totalGross, BigDecimal totalEmployeeDeductions,
                                       BigDecimal totalIncomeTax, BigDecimal totalNet,
                                       BigDecimal totalEmployerCharges, int nbEmployes,
                                       Instant calculatedAt, UUID validatedBy, Instant validatedAt,
                                       UUID approvedBy, Instant approvedAt, Instant paidAt, Instant closedAt,
                                       String rejectionReason, UUID rejectedBy, Instant rejectedAt) {
        return new PayrollRun(id, tenantId, createdAt, updatedAt, organizationId, agencyId, period, runType,
                status, currency, totalGross, totalEmployeeDeductions, totalIncomeTax, totalNet,
                totalEmployerCharges, nbEmployes, calculatedAt, validatedBy, validatedAt,
                approvedBy, approvedAt, paidAt, closedAt, rejectionReason, rejectedBy, rejectedAt);
    }

    public PayrollRun lockVariables() {
        requireStatus(PayrollRunStatus.DRAFT, "lock variables");
        return transition(PayrollRunStatus.VARIABLES_LOCKED);
    }

    /**
     * Records the computed totals and moves to CALCULATED. Allowed from DRAFT, VARIABLES_LOCKED,
     * REVIEW or REJECTED — the last enabling the payroll manager to recalculate after a rejection.
     */
    public PayrollRun markCalculated(PayrollRunTotals totals) {
        if (status != PayrollRunStatus.DRAFT && status != PayrollRunStatus.VARIABLES_LOCKED
                && status != PayrollRunStatus.REVIEW && status != PayrollRunStatus.REJECTED) {
            throw new IllegalStateException("Cannot calculate payroll run in status " + status);
        }
        Instant now = Instant.now();
        return new PayrollRun(id(), tenantId(), createdAt(), now, organizationId, agencyId, period, runType,
                PayrollRunStatus.CALCULATED, currency,
                totals.totalGross(), totals.totalEmployeeDeductions(), totals.totalIncomeTax(),
                totals.totalNet(), totals.totalEmployerCharges(), totals.nbEmployes(),
                now, validatedBy, validatedAt, approvedBy, approvedAt, paidAt, closedAt,
                rejectionReason, rejectedBy, rejectedAt);
    }

    public PayrollRun putInReview() {
        requireStatus(PayrollRunStatus.CALCULATED, "put in review");
        return transition(PayrollRunStatus.REVIEW);
    }

    public PayrollRun validate(UUID validatorId) {
        if (status != PayrollRunStatus.CALCULATED && status != PayrollRunStatus.REVIEW) {
            throw new IllegalStateException("Cannot validate payroll run in status " + status);
        }
        Instant now = Instant.now();
        return new PayrollRun(id(), tenantId(), createdAt(), now, organizationId, agencyId, period, runType,
                PayrollRunStatus.VALIDATED, currency, totalGross, totalEmployeeDeductions, totalIncomeTax,
                totalNet, totalEmployerCharges, nbEmployes, calculatedAt, validatorId, now,
                approvedBy, approvedAt, paidAt, closedAt, rejectionReason, rejectedBy, rejectedAt);
    }

    /**
     * HR admin sends a CALCULATED/REVIEW cycle back to the payroll manager with a mandatory
     * justification. The cycle becomes REJECTED until it is recalculated.
     */
    public PayrollRun reject(UUID rejectorId, String reason) {
        if (status != PayrollRunStatus.CALCULATED && status != PayrollRunStatus.REVIEW) {
            throw new IllegalStateException("Cannot reject payroll run in status " + status);
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A justification is required to reject a payroll cycle");
        }
        Instant now = Instant.now();
        return new PayrollRun(id(), tenantId(), createdAt(), now, organizationId, agencyId, period, runType,
                PayrollRunStatus.REJECTED, currency, totalGross, totalEmployeeDeductions, totalIncomeTax,
                totalNet, totalEmployerCharges, nbEmployes, calculatedAt, validatedBy, validatedAt,
                approvedBy, approvedAt, paidAt, closedAt, reason.strip(), rejectorId, now);
    }

    public PayrollRun approve(UUID approverId) {
        requireStatus(PayrollRunStatus.VALIDATED, "approve");
        Instant now = Instant.now();
        return new PayrollRun(id(), tenantId(), createdAt(), now, organizationId, agencyId, period, runType,
                PayrollRunStatus.APPROVED, currency, totalGross, totalEmployeeDeductions, totalIncomeTax,
                totalNet, totalEmployerCharges, nbEmployes, calculatedAt, validatedBy, validatedAt,
                approverId, now, paidAt, closedAt, rejectionReason, rejectedBy, rejectedAt);
    }

    public PayrollRun initiatePayment() {
        requireStatus(PayrollRunStatus.APPROVED, "initiate payment");
        return transition(PayrollRunStatus.PAYMENT_INITIATED);
    }

    public PayrollRun markPaid() {
        requireStatus(PayrollRunStatus.PAYMENT_INITIATED, "mark paid");
        Instant now = Instant.now();
        return new PayrollRun(id(), tenantId(), createdAt(), now, organizationId, agencyId, period, runType,
                PayrollRunStatus.PAID, currency, totalGross, totalEmployeeDeductions, totalIncomeTax,
                totalNet, totalEmployerCharges, nbEmployes, calculatedAt, validatedBy, validatedAt,
                approvedBy, approvedAt, now, closedAt, rejectionReason, rejectedBy, rejectedAt);
    }

    public PayrollRun close() {
        requireStatus(PayrollRunStatus.PAID, "close");
        Instant now = Instant.now();
        return new PayrollRun(id(), tenantId(), createdAt(), now, organizationId, agencyId, period, runType,
                PayrollRunStatus.CLOSED, currency, totalGross, totalEmployeeDeductions, totalIncomeTax,
                totalNet, totalEmployerCharges, nbEmployes, calculatedAt, validatedBy, validatedAt,
                approvedBy, approvedAt, paidAt, now, rejectionReason, rejectedBy, rejectedAt);
    }

    private PayrollRun transition(PayrollRunStatus newStatus) {
        return new PayrollRun(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId, period,
                runType, newStatus, currency, totalGross, totalEmployeeDeductions, totalIncomeTax,
                totalNet, totalEmployerCharges, nbEmployes, calculatedAt, validatedBy, validatedAt,
                approvedBy, approvedAt, paidAt, closedAt, rejectionReason, rejectedBy, rejectedAt);
    }

    private void requireStatus(PayrollRunStatus expected, String action) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Cannot " + action + " payroll run in status " + status + " (expected " + expected + ")");
        }
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public PayPeriod period() { return period; }
    public RunType runType() { return runType; }
    public PayrollRunStatus status() { return status; }
    public String currency() { return currency; }
    public BigDecimal totalGross() { return totalGross; }
    public BigDecimal totalEmployeeDeductions() { return totalEmployeeDeductions; }
    public BigDecimal totalIncomeTax() { return totalIncomeTax; }
    public BigDecimal totalNet() { return totalNet; }
    public BigDecimal totalEmployerCharges() { return totalEmployerCharges; }
    public int nbEmployes() { return nbEmployes; }
    public Instant calculatedAt() { return calculatedAt; }
    public UUID validatedBy() { return validatedBy; }
    public Instant validatedAt() { return validatedAt; }
    public UUID approvedBy() { return approvedBy; }
    public Instant approvedAt() { return approvedAt; }
    public Instant paidAt() { return paidAt; }
    public Instant closedAt() { return closedAt; }
    public String rejectionReason() { return rejectionReason; }
    public UUID rejectedBy() { return rejectedBy; }
    public Instant rejectedAt() { return rejectedAt; }
}
