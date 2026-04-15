package yowyob.comops.api.inventory.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import yowyob.comops.api.inventory.domain.InvalidWarehouseTransferStateException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class WarehouseTransfer extends BaseEntity {
    private static final String STATUS_REQUESTED = "REQUESTED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final Set<String> ALLOWED_STATUSES = Set.of(STATUS_REQUESTED, STATUS_COMPLETED);
    private final UUID organizationId;
    private final UUID sourceAgencyId;
    private final UUID targetAgencyId;
    private final UUID productId;
    private final String referenceNumber;
    private final BigDecimal quantity;
    private final String status;

    private WarehouseTransfer(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID sourceAgencyId, UUID targetAgencyId, UUID productId, String referenceNumber, BigDecimal quantity,
            String status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.sourceAgencyId = requireUuid(sourceAgencyId, "sourceAgencyId");
        this.targetAgencyId = requireUuid(targetAgencyId, "targetAgencyId");
        if (sourceAgencyId.equals(targetAgencyId)) {
            throw new IllegalArgumentException("sourceAgencyId and targetAgencyId must be different");
        }
        this.productId = requireUuid(productId, "productId");
        this.referenceNumber = requireText(referenceNumber, "referenceNumber").toUpperCase();
        this.quantity = requirePositive(quantity, "quantity");
        this.status = normalizeStatus(status);
    }

    public static WarehouseTransfer create(UUID tenantId, UUID organizationId, UUID sourceAgencyId, UUID targetAgencyId,
            UUID productId, String referenceNumber, BigDecimal quantity) {
        Instant now = Instant.now();
        return new WarehouseTransfer(UUID.randomUUID(), tenantId, now, now, organizationId, sourceAgencyId, targetAgencyId,
                productId, referenceNumber, quantity, STATUS_REQUESTED);
    }

    public static WarehouseTransfer rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID sourceAgencyId, UUID targetAgencyId, UUID productId, String referenceNumber,
            BigDecimal quantity, String status) {
        return new WarehouseTransfer(id, tenantId, createdAt, updatedAt, organizationId, sourceAgencyId, targetAgencyId,
                productId, referenceNumber, quantity, status);
    }

    public UUID organizationId() { return organizationId; }
    public UUID sourceAgencyId() { return sourceAgencyId; }
    public UUID targetAgencyId() { return targetAgencyId; }
    public UUID productId() { return productId; }
    public String referenceNumber() { return referenceNumber; }
    public BigDecimal quantity() { return quantity; }
    public String status() { return status; }
    public boolean completed() { return STATUS_COMPLETED.equals(status); }
    public WarehouseTransfer complete() {
        if (!STATUS_REQUESTED.equals(status)) {
            throw new InvalidWarehouseTransferStateException(id(), status, STATUS_REQUESTED);
        }
        return new WarehouseTransfer(id(), tenantId(), createdAt(), Instant.now(), organizationId, sourceAgencyId,
                targetAgencyId, productId, referenceNumber, quantity, STATUS_COMPLETED);
    }

    private static UUID requireUuid(UUID value, String field) { if (value == null) throw new IllegalArgumentException(field + " is required"); return value; }
    private static String requireText(String value, String field) { if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required"); return value.trim(); }
    private static BigDecimal requirePositive(BigDecimal value, String field) { if (value == null || value.signum() <= 0) throw new IllegalArgumentException(field + " must be positive"); return value; }
    private static String normalizeStatus(String value) {
        String normalized = requireText(value, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("status must be one of " + ALLOWED_STATUSES);
        }
        return normalized;
    }
}
