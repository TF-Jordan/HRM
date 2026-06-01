package yowyob.comops.api.sales.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import yowyob.comops.api.sales.domain.InvalidSalesOrderStateException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.UUID;

public final class SalesOrder extends BaseEntity {
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final Set<String> ALLOWED_STATUSES = Set.of(STATUS_DRAFT, STATUS_CONFIRMED, STATUS_CANCELLED);

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID customerThirdPartyId;
    private final String orderNumber;
    private final String currency;
    private final String status;
    private final List<SalesOrderLine> lines;
    private final BigDecimal totalQuantity;
    private final BigDecimal subtotalAmount;
    private final BigDecimal totalAmount;

    private SalesOrder(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId, UUID agencyId,
            UUID customerThirdPartyId, String orderNumber, String currency, String status, List<SalesOrderLine> lines,
            BigDecimal totalQuantity, BigDecimal subtotalAmount, BigDecimal totalAmount) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.agencyId = Objects.requireNonNull(agencyId, "agencyId is required");
        this.customerThirdPartyId = Objects.requireNonNull(customerThirdPartyId, "customerThirdPartyId is required");
        this.orderNumber = requireText(orderNumber, "orderNumber");
        this.currency = normalizeCurrency(currency);
        this.status = normalizeStatus(status);
        this.lines = normalizeLines(lines);
        this.totalQuantity = requirePositive(totalQuantity, "totalQuantity");
        this.subtotalAmount = requirePositive(subtotalAmount, "subtotalAmount");
        this.totalAmount = requirePositive(totalAmount, "totalAmount");
    }

    public static SalesOrder create(UUID tenantId, UUID organizationId, UUID agencyId, UUID customerThirdPartyId,
            String orderNumber, List<SalesOrderLine> lines, String currency) {
        Instant now = Instant.now();
        BigDecimal totalQuantity = lines.stream().map(SalesOrderLine::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subtotalAmount = lines.stream().map(SalesOrderLine::lineAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SalesOrder(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, customerThirdPartyId,
                orderNumber, currency, STATUS_DRAFT, lines, totalQuantity, subtotalAmount, subtotalAmount);
    }

    public static SalesOrder rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, UUID customerThirdPartyId, String orderNumber, String currency,
            String status, List<SalesOrderLine> lines, BigDecimal totalQuantity, BigDecimal subtotalAmount,
            BigDecimal totalAmount) {
        return new SalesOrder(id, tenantId, createdAt, updatedAt, organizationId, agencyId, customerThirdPartyId,
                orderNumber, currency, status, lines, totalQuantity, subtotalAmount, totalAmount);
    }

    public SalesOrder confirm() {
        if (!STATUS_DRAFT.equals(status)) {
            throw new InvalidSalesOrderStateException(id(), status, STATUS_DRAFT);
        }
        return new SalesOrder(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId, customerThirdPartyId,
                orderNumber, currency, STATUS_CONFIRMED, lines, totalQuantity, subtotalAmount, totalAmount);
    }

    public SalesOrder update(UUID organizationId, UUID agencyId, UUID customerThirdPartyId, String orderNumber,
            List<SalesOrderLine> lines, String currency) {
        if (!STATUS_DRAFT.equals(status)) {
            throw new InvalidSalesOrderStateException(id(), status, STATUS_DRAFT);
        }
        BigDecimal nextTotalQuantity = lines.stream().map(SalesOrderLine::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal nextSubtotalAmount = lines.stream().map(SalesOrderLine::lineAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SalesOrder(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId, customerThirdPartyId,
                orderNumber, currency, STATUS_DRAFT, lines, nextTotalQuantity, nextSubtotalAmount, nextSubtotalAmount);
    }

    public SalesOrder cancel() {
        if (!STATUS_DRAFT.equals(status)) {
            throw new InvalidSalesOrderStateException(id(), status, STATUS_DRAFT);
        }
        return new SalesOrder(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId, customerThirdPartyId,
                orderNumber, currency, STATUS_CANCELLED, lines, totalQuantity, subtotalAmount, totalAmount);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID customerThirdPartyId() { return customerThirdPartyId; }
    public UUID productId() { return lines.getFirst().productId(); }
    public String orderNumber() { return orderNumber; }
    public BigDecimal quantity() { return lines.getFirst().quantity(); }
    public BigDecimal unitPrice() { return lines.getFirst().unitPrice(); }
    public String currency() { return currency; }
    public String status() { return status; }
    public List<SalesOrderLine> lines() { return lines; }
    public BigDecimal totalQuantity() { return totalQuantity; }
    public BigDecimal subtotalAmount() { return subtotalAmount; }
    public BigDecimal totalAmount() { return totalAmount; }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
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

    private static BigDecimal requirePositive(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " is required");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }

    private static List<SalesOrderLine> normalizeLines(List<SalesOrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("at least one sales order line is required");
        }
        return List.copyOf(lines);
    }
}
