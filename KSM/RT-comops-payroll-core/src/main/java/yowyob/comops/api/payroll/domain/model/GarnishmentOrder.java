package yowyob.comops.api.payroll.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A wage-garnishment order against an employee: a beneficiary is owed {@code totalAmount},
 * withheld in {@code monthlyAmount} installments until {@code remainingBalance} reaches zero.
 * Its {@link GarnishmentType} sets its legal priority.
 */
public final class GarnishmentOrder extends BaseEntity {

    private final UUID organizationId;
    private final UUID employeeId;
    private final GarnishmentType type;
    private final String beneficiary;
    private final String reference;
    private final BigDecimal totalAmount;
    private final BigDecimal remainingBalance;
    private final BigDecimal monthlyAmount;
    private final GarnishmentStatus status;

    private GarnishmentOrder(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                             UUID organizationId, UUID employeeId, GarnishmentType type, String beneficiary,
                             String reference, BigDecimal totalAmount, BigDecimal remainingBalance,
                             BigDecimal monthlyAmount, GarnishmentStatus status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.beneficiary = Objects.requireNonNull(beneficiary, "beneficiary is required");
        this.totalAmount = Objects.requireNonNull(totalAmount, "totalAmount is required");
        this.remainingBalance = Objects.requireNonNull(remainingBalance, "remainingBalance is required");
        this.monthlyAmount = Objects.requireNonNull(monthlyAmount, "monthlyAmount is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.reference = reference;
    }

    public static GarnishmentOrder create(UUID tenantId, UUID organizationId, UUID employeeId,
                                          GarnishmentType type, String beneficiary, String reference,
                                          BigDecimal totalAmount, BigDecimal monthlyAmount) {
        Instant now = Instant.now();
        return new GarnishmentOrder(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId,
                type, beneficiary, reference, totalAmount, totalAmount, monthlyAmount,
                GarnishmentStatus.ACTIVE);
    }

    public static GarnishmentOrder rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                             UUID organizationId, UUID employeeId, GarnishmentType type,
                                             String beneficiary, String reference, BigDecimal totalAmount,
                                             BigDecimal remainingBalance, BigDecimal monthlyAmount,
                                             GarnishmentStatus status) {
        return new GarnishmentOrder(id, tenantId, createdAt, updatedAt, organizationId, employeeId, type,
                beneficiary, reference, totalAmount, remainingBalance, monthlyAmount, status);
    }

    /** The amount due this period: the installment capped at the remaining balance. */
    public BigDecimal installmentDue() {
        return monthlyAmount.min(remainingBalance).max(BigDecimal.ZERO);
    }

    /** Applies a withheld amount, decrementing the balance and completing the order at zero. */
    public GarnishmentOrder applyDeduction(BigDecimal amount) {
        BigDecimal applied = amount == null ? BigDecimal.ZERO : amount.min(remainingBalance);
        BigDecimal newBalance = remainingBalance.subtract(applied).max(BigDecimal.ZERO);
        GarnishmentStatus newStatus = newBalance.signum() == 0 ? GarnishmentStatus.COMPLETED : status;
        return new GarnishmentOrder(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                type, beneficiary, reference, totalAmount, newBalance, monthlyAmount, newStatus);
    }

    public GarnishmentOrder cancel() {
        return new GarnishmentOrder(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                type, beneficiary, reference, totalAmount, remainingBalance, monthlyAmount,
                GarnishmentStatus.CANCELLED);
    }

    /** Temporarily stops withholding. Only an active order can be suspended. */
    public GarnishmentOrder suspend() {
        if (status != GarnishmentStatus.ACTIVE) {
            throw new IllegalStateException("Only an active garnishment can be suspended (was " + status + ").");
        }
        return new GarnishmentOrder(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                type, beneficiary, reference, totalAmount, remainingBalance, monthlyAmount,
                GarnishmentStatus.SUSPENDED);
    }

    /** Resumes withholding on a suspended order. */
    public GarnishmentOrder resume() {
        if (status != GarnishmentStatus.SUSPENDED) {
            throw new IllegalStateException("Only a suspended garnishment can be resumed (was " + status + ").");
        }
        return new GarnishmentOrder(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                type, beneficiary, reference, totalAmount, remainingBalance, monthlyAmount,
                GarnishmentStatus.ACTIVE);
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public GarnishmentType type() { return type; }
    public String beneficiary() { return beneficiary; }
    public String reference() { return reference; }
    public BigDecimal totalAmount() { return totalAmount; }
    public BigDecimal remainingBalance() { return remainingBalance; }
    public BigDecimal monthlyAmount() { return monthlyAmount; }
    public GarnishmentStatus status() { return status; }
}
