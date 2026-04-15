package yowyob.comops.api.product.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Product extends BaseEntity {
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";
    private static final Set<String> ALLOWED_STATUSES = Set.of(STATUS_ACTIVE, STATUS_INACTIVE);

    private final UUID organizationId;
    private final String sku;
    private final String name;
    private final String familyCode;
    private final String categoryCode;
    private final String variantLabel;
    private final String barcode;
    private final String description;
    private final BigDecimal unitPrice;
    private final String currency;
    private final String status;

    private Product(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId, String sku,
            String name, String familyCode, String categoryCode, String variantLabel, String barcode, String description, BigDecimal unitPrice,
            String currency, String status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.sku = normalizeSku(sku);
        this.name = requireText(name, "name");
        this.familyCode = normalizeCode(familyCode, "familyCode");
        this.categoryCode = normalizeOptionalCode(categoryCode);
        this.variantLabel = requireText(variantLabel, "variantLabel");
        this.barcode = normalizeOptionalText(barcode);
        this.description = normalizeOptionalText(description);
        this.unitPrice = requirePositive(unitPrice, "unitPrice");
        this.currency = normalizeCurrency(currency);
        this.status = normalizeStatus(status);
    }

    public static Product create(UUID tenantId, UUID organizationId, String sku, String name, String familyCode,
            String categoryCode, String variantLabel, String barcode, String description, BigDecimal unitPrice, String currency, String status) {
        Instant now = Instant.now();
        return new Product(UUID.randomUUID(), tenantId, now, now, organizationId, sku, name, familyCode, categoryCode, variantLabel,
                barcode, description, unitPrice, currency, status == null || status.isBlank() ? STATUS_ACTIVE : status);
    }

    public static Product rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            String sku, String name, String familyCode, String categoryCode, String variantLabel, String barcode, String description,
            BigDecimal unitPrice, String currency, String status) {
        return new Product(id, tenantId, createdAt, updatedAt, organizationId, sku, name, familyCode, categoryCode, variantLabel,
                barcode, description, unitPrice, currency, status);
    }

    public Product update(String sku, String name, String familyCode, String categoryCode, String variantLabel, String barcode,
            String description, BigDecimal unitPrice, String currency, String status) {
        return new Product(id(), tenantId(), createdAt(), Instant.now(), organizationId, sku, name, familyCode, categoryCode,
                variantLabel, barcode, description, unitPrice, currency, status);
    }

    public UUID organizationId() { return organizationId; }
    public String sku() { return sku; }
    public String name() { return name; }
    public String familyCode() { return familyCode; }
    public String categoryCode() { return categoryCode; }
    public String variantLabel() { return variantLabel; }
    public String barcode() { return barcode; }
    public String description() { return description; }
    public BigDecimal unitPrice() { return unitPrice; }
    public String currency() { return currency; }
    public String status() { return status; }
    public boolean active() { return STATUS_ACTIVE.equals(status); }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalizeSku(String value) {
        return requireText(value, "sku").toUpperCase();
    }

    private static String normalizeCode(String value, String field) {
        return requireText(value, field).toUpperCase();
    }

    private static String normalizeOptionalCode(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }

    private static String normalizeCurrency(String value) {
        return requireText(value, "currency").toUpperCase();
    }

    private static String normalizeStatus(String value) {
        String normalized = requireText(value, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("status must be one of " + ALLOWED_STATUSES);
        }
        return normalized;
    }

    private static String normalizeOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static BigDecimal requirePositive(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " is required");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }
}
