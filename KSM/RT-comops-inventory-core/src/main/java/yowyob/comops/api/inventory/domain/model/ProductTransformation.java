package yowyob.comops.api.inventory.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class ProductTransformation extends BaseEntity {
    private static final java.util.Set<String> ALLOWED_STATUSES = java.util.Set.of("DRAFT", "VALIDATED");
    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID sourceProductId;
    private final UUID targetProductId;
    private final String referenceNumber;
    private final BigDecimal sourceQuantity;
    private final BigDecimal targetQuantity;
    private final String status;

    private ProductTransformation(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID agencyId, UUID sourceProductId, UUID targetProductId, String referenceNumber, BigDecimal sourceQuantity,
            BigDecimal targetQuantity, String status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.agencyId = requireUuid(agencyId, "agencyId");
        this.sourceProductId = requireUuid(sourceProductId, "sourceProductId");
        this.targetProductId = requireUuid(targetProductId, "targetProductId");
        if (sourceProductId.equals(targetProductId)) {
            throw new IllegalArgumentException("sourceProductId and targetProductId must be different");
        }
        this.referenceNumber = requireText(referenceNumber, "referenceNumber").toUpperCase();
        this.sourceQuantity = requirePositive(sourceQuantity, "sourceQuantity");
        this.targetQuantity = requirePositive(targetQuantity, "targetQuantity");
        this.status = normalizeStatus(status);
    }

    public static ProductTransformation record(UUID tenantId, UUID organizationId, UUID agencyId, UUID sourceProductId,
            UUID targetProductId, String referenceNumber, BigDecimal sourceQuantity, BigDecimal targetQuantity) {
        return recordValidated(tenantId, organizationId, agencyId, sourceProductId, targetProductId, referenceNumber,
                sourceQuantity, targetQuantity);
    }

    public static ProductTransformation recordDraft(UUID tenantId, UUID organizationId, UUID agencyId, UUID sourceProductId,
            UUID targetProductId, String referenceNumber, BigDecimal sourceQuantity, BigDecimal targetQuantity) {
        Instant now = Instant.now();
        return new ProductTransformation(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, sourceProductId,
                targetProductId, referenceNumber, sourceQuantity, targetQuantity, "DRAFT");
    }

    public static ProductTransformation recordValidated(UUID tenantId, UUID organizationId, UUID agencyId, UUID sourceProductId,
            UUID targetProductId, String referenceNumber, BigDecimal sourceQuantity, BigDecimal targetQuantity) {
        Instant now = Instant.now();
        return new ProductTransformation(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, sourceProductId,
                targetProductId, referenceNumber, sourceQuantity, targetQuantity, "VALIDATED");
    }

    public static ProductTransformation rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, UUID sourceProductId, UUID targetProductId, String referenceNumber,
            BigDecimal sourceQuantity, BigDecimal targetQuantity, String status) {
        return new ProductTransformation(id, tenantId, createdAt, updatedAt, organizationId, agencyId, sourceProductId,
                targetProductId, referenceNumber, sourceQuantity, targetQuantity, status);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID sourceProductId() { return sourceProductId; }
    public UUID targetProductId() { return targetProductId; }
    public String referenceNumber() { return referenceNumber; }
    public BigDecimal sourceQuantity() { return sourceQuantity; }
    public BigDecimal targetQuantity() { return targetQuantity; }
    public String status() { return status; }
    public boolean validated() { return "VALIDATED".equals(status); }

    public ProductTransformation validate() {
        if (validated()) {
            return this;
        }
        return new ProductTransformation(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                sourceProductId, targetProductId, referenceNumber, sourceQuantity, targetQuantity, "VALIDATED");
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
