package yowyob.comops.api.product.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class Batch extends BaseEntity {

    private final UUID productId;
    private final String lotNumber;
    private final LocalDate manufacturingDate;
    private final LocalDate expiryDate;
    private final int quantity;

    private Batch(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID productId, String lotNumber,
            LocalDate manufacturingDate, LocalDate expiryDate, int quantity) {
        super(id, tenantId, createdAt, updatedAt);
        this.productId = requireUuid(productId, "productId");
        this.lotNumber = requireText(lotNumber, "lotNumber").toUpperCase();
        this.manufacturingDate = manufacturingDate;
        this.expiryDate = expiryDate;
        this.quantity = requirePositive(quantity);
        if (manufacturingDate != null && expiryDate != null && expiryDate.isBefore(manufacturingDate)) {
            throw new IllegalArgumentException("expiryDate must be after manufacturingDate");
        }
    }

    public static Batch create(UUID tenantId, UUID productId, String lotNumber, LocalDate manufacturingDate,
            LocalDate expiryDate, int quantity) {
        Instant now = Instant.now();
        return new Batch(UUID.randomUUID(), tenantId, now, now, productId, lotNumber, manufacturingDate, expiryDate,
                quantity);
    }

    public static Batch rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID productId,
            String lotNumber, LocalDate manufacturingDate, LocalDate expiryDate, int quantity) {
        return new Batch(id, tenantId, createdAt, updatedAt, productId, lotNumber, manufacturingDate, expiryDate,
                quantity);
    }

    public UUID productId() { return productId; }
    public String lotNumber() { return lotNumber; }
    public LocalDate manufacturingDate() { return manufacturingDate; }
    public LocalDate expiryDate() { return expiryDate; }
    public int quantity() { return quantity; }
    public boolean isExpired(LocalDate at) { return expiryDate != null && expiryDate.isBefore(at); }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static int requirePositive(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        return quantity;
    }
}
