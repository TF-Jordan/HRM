package yowyob.comops.api.inventory.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class InventorySession extends BaseEntity {

    private static final Set<String> ALLOWED_STATUSES = Set.of("DRAFT", "VALIDATED");

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID productId;
    private final String referenceNumber;
    private final BigDecimal countedQuantity;
    private final String status;

    private InventorySession(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID agencyId, UUID productId, String referenceNumber, BigDecimal countedQuantity, String status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.agencyId = requireUuid(agencyId, "agencyId");
        this.productId = requireUuid(productId, "productId");
        this.referenceNumber = requireText(referenceNumber, "referenceNumber").toUpperCase();
        this.countedQuantity = requirePositive(countedQuantity, "countedQuantity");
        this.status = normalizeStatus(status);
    }

    public static InventorySession create(UUID tenantId, UUID organizationId, UUID agencyId, UUID productId,
            String referenceNumber, BigDecimal countedQuantity) {
        Instant now = Instant.now();
        return new InventorySession(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, productId,
                referenceNumber, countedQuantity, "DRAFT");
    }

    public static InventorySession rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, UUID productId, String referenceNumber, BigDecimal countedQuantity,
            String status) {
        return new InventorySession(id, tenantId, createdAt, updatedAt, organizationId, agencyId, productId,
                referenceNumber, countedQuantity, status);
    }

    public InventorySession validate() {
        if ("VALIDATED".equals(status)) {
            return this;
        }
        return new InventorySession(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId, productId,
                referenceNumber, countedQuantity, "VALIDATED");
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID productId() { return productId; }
    public String referenceNumber() { return referenceNumber; }
    public BigDecimal countedQuantity() { return countedQuantity; }
    public String status() { return status; }

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
