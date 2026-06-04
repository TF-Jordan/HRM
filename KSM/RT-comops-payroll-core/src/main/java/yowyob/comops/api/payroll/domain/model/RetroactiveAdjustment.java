package yowyob.comops.api.payroll.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A retroactive pay adjustment: a past period ({@code originPeriod}) is recomputed with a new
 * salary, and the resulting deltas are paid in a later period ({@code targetPeriod}).
 */
public final class RetroactiveAdjustment extends BaseEntity {

    private final UUID organizationId;
    private final UUID employeeId;
    private final String originPeriod;
    private final String targetPeriod;
    private final String reason;
    private final String currency;
    private final BigDecimal oldGross;
    private final BigDecimal newGross;
    private final BigDecimal deltaGross;
    private final BigDecimal oldNet;
    private final BigDecimal newNet;
    private final BigDecimal deltaNet;
    private final RetroactiveStatus status;

    private RetroactiveAdjustment(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                  UUID organizationId, UUID employeeId, String originPeriod,
                                  String targetPeriod, String reason, String currency, BigDecimal oldGross,
                                  BigDecimal newGross, BigDecimal deltaGross, BigDecimal oldNet,
                                  BigDecimal newNet, BigDecimal deltaNet, RetroactiveStatus status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.originPeriod = Objects.requireNonNull(originPeriod, "originPeriod is required");
        this.targetPeriod = Objects.requireNonNull(targetPeriod, "targetPeriod is required");
        this.currency = Objects.requireNonNull(currency, "currency is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.reason = reason;
        this.oldGross = oldGross;
        this.newGross = newGross;
        this.deltaGross = deltaGross;
        this.oldNet = oldNet;
        this.newNet = newNet;
        this.deltaNet = deltaNet;
    }

    public static RetroactiveAdjustment create(UUID tenantId, UUID organizationId, UUID employeeId,
                                               String originPeriod, String targetPeriod, String reason,
                                               String currency, BigDecimal oldGross, BigDecimal newGross,
                                               BigDecimal deltaGross, BigDecimal oldNet, BigDecimal newNet,
                                               BigDecimal deltaNet) {
        Instant now = Instant.now();
        return new RetroactiveAdjustment(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId,
                originPeriod, targetPeriod, reason, currency, oldGross, newGross, deltaGross, oldNet, newNet,
                deltaNet, RetroactiveStatus.PENDING);
    }

    public static RetroactiveAdjustment rehydrate(UUID id, UUID tenantId, Instant createdAt,
                                                  Instant updatedAt, UUID organizationId, UUID employeeId,
                                                  String originPeriod, String targetPeriod, String reason,
                                                  String currency, BigDecimal oldGross, BigDecimal newGross,
                                                  BigDecimal deltaGross, BigDecimal oldNet, BigDecimal newNet,
                                                  BigDecimal deltaNet, RetroactiveStatus status) {
        return new RetroactiveAdjustment(id, tenantId, createdAt, updatedAt, organizationId, employeeId,
                originPeriod, targetPeriod, reason, currency, oldGross, newGross, deltaGross, oldNet, newNet,
                deltaNet, status);
    }

    public RetroactiveAdjustment markApplied() {
        if (status != RetroactiveStatus.PENDING) {
            throw new IllegalStateException("Cannot apply retroactive adjustment in status " + status);
        }
        return transition(RetroactiveStatus.APPLIED);
    }

    public RetroactiveAdjustment cancel() {
        if (status != RetroactiveStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel retroactive adjustment in status " + status);
        }
        return transition(RetroactiveStatus.CANCELLED);
    }

    private RetroactiveAdjustment transition(RetroactiveStatus newStatus) {
        return new RetroactiveAdjustment(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                employeeId, originPeriod, targetPeriod, reason, currency, oldGross, newGross, deltaGross,
                oldNet, newNet, deltaNet, newStatus);
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public String originPeriod() { return originPeriod; }
    public String targetPeriod() { return targetPeriod; }
    public String reason() { return reason; }
    public String currency() { return currency; }
    public BigDecimal oldGross() { return oldGross; }
    public BigDecimal newGross() { return newGross; }
    public BigDecimal deltaGross() { return deltaGross; }
    public BigDecimal oldNet() { return oldNet; }
    public BigDecimal newNet() { return newNet; }
    public BigDecimal deltaNet() { return deltaNet; }
    public RetroactiveStatus status() { return status; }
}
