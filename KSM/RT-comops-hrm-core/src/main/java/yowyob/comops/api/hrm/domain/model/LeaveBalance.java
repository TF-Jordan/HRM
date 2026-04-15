package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class LeaveBalance extends BaseEntity {

    private final UUID organizationId;
    private final UUID employeeId;
    private final LeaveType type;
    private final BigDecimal acquis;
    private final BigDecimal pris;
    private final int annee;

    private LeaveBalance(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                         UUID organizationId, UUID employeeId, LeaveType type,
                         BigDecimal acquis, BigDecimal pris, int annee) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.acquis = Objects.requireNonNull(acquis, "acquis is required");
        this.pris = Objects.requireNonNull(pris, "pris is required");
        this.annee = annee;
    }

    public static LeaveBalance initialize(UUID tenantId, UUID organizationId, UUID employeeId,
                                          LeaveType type, int annee) {
        Instant now = Instant.now();
        return new LeaveBalance(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId,
                type, BigDecimal.ZERO, BigDecimal.ZERO, annee);
    }

    public static LeaveBalance rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                         UUID organizationId, UUID employeeId, LeaveType type,
                                         BigDecimal acquis, BigDecimal pris, int annee) {
        return new LeaveBalance(id, tenantId, createdAt, updatedAt, organizationId, employeeId,
                type, acquis, pris, annee);
    }

    public LeaveBalance crediter(BigDecimal jours) {
        return new LeaveBalance(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                employeeId, type, acquis.add(jours), pris, annee);
    }

    public LeaveBalance debiter(BigDecimal jours) {
        if (soldeRestant().compareTo(jours) < 0) {
            throw new IllegalStateException("Insufficient leave balance");
        }
        return new LeaveBalance(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                employeeId, type, acquis, pris.add(jours), annee);
    }

    public BigDecimal soldeRestant() {
        return acquis.subtract(pris);
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public LeaveType type() { return type; }
    public BigDecimal acquis() { return acquis; }
    public BigDecimal pris() { return pris; }
    public int annee() { return annee; }
}
