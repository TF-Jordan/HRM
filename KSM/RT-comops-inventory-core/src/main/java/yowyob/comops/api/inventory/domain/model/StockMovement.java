package yowyob.comops.api.inventory.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class StockMovement extends BaseEntity {
    private static final Set<String> ALLOWED_MOVEMENT_TYPES = Set.of(
            "INBOUND",
            "OUTBOUND",
            "ADJUSTMENT_IN",
            "ADJUSTMENT_OUT");
    private static final Set<String> ALLOWED_STATUSES = Set.of("DRAFT", "VALIDATED");

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID productId;
    private final UUID thirdPartyId;
    private final String referenceNumber;
    private final String sourceDocumentType;
    private final String sourceDocumentNumber;
    private final String movementType;
    private final BigDecimal quantity;
    private final String status;

    private StockMovement(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID agencyId, UUID productId, UUID thirdPartyId, String referenceNumber, String sourceDocumentType,
            String sourceDocumentNumber, String movementType, BigDecimal quantity, String status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.agencyId = Objects.requireNonNull(agencyId, "agencyId is required");
        this.productId = Objects.requireNonNull(productId, "productId is required");
        this.thirdPartyId = thirdPartyId;
        this.referenceNumber = normalizeReference(referenceNumber);
        this.sourceDocumentType = normalizeOptionalText(sourceDocumentType);
        this.sourceDocumentNumber = normalizeOptionalText(sourceDocumentNumber);
        this.movementType = normalizeMovementType(movementType);
        this.quantity = requirePositive(quantity, "quantity");
        this.status = normalizeStatus(status);
    }

    public static StockMovement record(UUID tenantId, UUID organizationId, UUID agencyId, UUID productId,
            UUID thirdPartyId, String referenceNumber, String sourceDocumentType, String sourceDocumentNumber,
            String movementType, BigDecimal quantity) {
        return recordValidated(tenantId, organizationId, agencyId, productId, thirdPartyId, referenceNumber,
                sourceDocumentType, sourceDocumentNumber, movementType, quantity);
    }

    public static StockMovement recordDraft(UUID tenantId, UUID organizationId, UUID agencyId, UUID productId,
            UUID thirdPartyId, String referenceNumber, String sourceDocumentType, String sourceDocumentNumber,
            String movementType, BigDecimal quantity) {
        Instant now = Instant.now();
        return new StockMovement(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, productId,
                thirdPartyId, referenceNumber, sourceDocumentType, sourceDocumentNumber, movementType, quantity, "DRAFT");
    }

    public static StockMovement recordValidated(UUID tenantId, UUID organizationId, UUID agencyId, UUID productId,
            UUID thirdPartyId, String referenceNumber, String sourceDocumentType, String sourceDocumentNumber,
            String movementType, BigDecimal quantity) {
        Instant now = Instant.now();
        return new StockMovement(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, productId,
                thirdPartyId, referenceNumber, sourceDocumentType, sourceDocumentNumber, movementType, quantity, "VALIDATED");
    }

    public static StockMovement rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, UUID productId, UUID thirdPartyId, String referenceNumber,
            String sourceDocumentType, String sourceDocumentNumber, String movementType, BigDecimal quantity, String status) {
        return new StockMovement(id, tenantId, createdAt, updatedAt, organizationId, agencyId, productId,
                thirdPartyId, referenceNumber, sourceDocumentType, sourceDocumentNumber, movementType, quantity, status);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID productId() { return productId; }
    public UUID thirdPartyId() { return thirdPartyId; }
    public String referenceNumber() { return referenceNumber; }
    public String sourceDocumentType() { return sourceDocumentType; }
    public String sourceDocumentNumber() { return sourceDocumentNumber; }
    public String movementType() { return movementType; }
    public BigDecimal quantity() { return quantity; }
    public String status() { return status; }
    public boolean validated() { return "VALIDATED".equals(status); }
    public BigDecimal signedQuantity() {
        return switch (movementType) {
            case "INBOUND", "ADJUSTMENT_IN" -> quantity;
            case "OUTBOUND", "ADJUSTMENT_OUT" -> quantity.negate();
            default -> throw new IllegalStateException("Unsupported movementType " + movementType);
        };
    }

    public StockMovement validate() {
        if (validated()) {
            return this;
        }
        return new StockMovement(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId, productId,
                thirdPartyId, referenceNumber, sourceDocumentType, sourceDocumentNumber, movementType, quantity,
                "VALIDATED");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalizeReference(String value) {
        return requireText(value, "referenceNumber").toUpperCase();
    }

    private static String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private static String normalizeMovementType(String value) {
        String normalized = requireText(value, "movementType").toUpperCase();
        if (!ALLOWED_MOVEMENT_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("movementType must be one of " + ALLOWED_MOVEMENT_TYPES);
        }
        return normalized;
    }

    private static String normalizeStatus(String value) {
        String normalized = requireText(value, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("status must be one of " + ALLOWED_STATUSES);
        }
        return normalized;
    }

    private static BigDecimal requirePositive(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " is required");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }
}
