package yowyob.comops.api.accounting.domain.model;

import yowyob.comops.api.accounting.domain.InvalidInvoiceStateException;
import yowyob.comops.api.common.domain.model.BaseEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Invoice extends BaseEntity {
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_POSTED = "POSTED";
    private static final Set<String> ALLOWED_STATUSES = Set.of(STATUS_DRAFT, STATUS_POSTED);
    private static final String PAYMENT_STATUS_UNPAID = "UNPAID";
    private static final String PAYMENT_STATUS_PARTIALLY_PAID = "PARTIALLY_PAID";
    private static final String PAYMENT_STATUS_PAID = "PAID";
    private static final Set<String> ALLOWED_PAYMENT_STATUSES = Set.of(
            PAYMENT_STATUS_UNPAID,
            PAYMENT_STATUS_PARTIALLY_PAID,
            PAYMENT_STATUS_PAID);

    private final UUID organizationId;
    private final UUID customerThirdPartyId;
    private final UUID orderId;
    private final String invoiceNumber;
    private final String currency;
    private final String status;
    private final String paymentStatus;
    private final List<InvoiceLine> lines;
    private final BigDecimal totalQuantity;
    private final BigDecimal subtotalAmount;
    private final BigDecimal totalAmount;
    private final BigDecimal settledAmount;
    private final BigDecimal outstandingAmount;
    private final Instant settledAt;

    private Invoice(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID customerThirdPartyId, UUID orderId, String invoiceNumber, String currency, String status,
            String paymentStatus, List<InvoiceLine> lines, BigDecimal totalQuantity, BigDecimal subtotalAmount,
            BigDecimal totalAmount, BigDecimal settledAmount, BigDecimal outstandingAmount, Instant settledAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.customerThirdPartyId = Objects.requireNonNull(customerThirdPartyId, "customerThirdPartyId is required");
        this.orderId = orderId;
        this.invoiceNumber = requireText(invoiceNumber, "invoiceNumber");
        this.currency = normalizeCurrency(currency);
        this.status = normalizeStatus(status);
        this.paymentStatus = normalizePaymentStatus(paymentStatus);
        this.lines = normalizeLines(lines);
        this.totalQuantity = requirePositive(totalQuantity, "totalQuantity");
        this.subtotalAmount = requirePositive(subtotalAmount, "subtotalAmount");
        this.totalAmount = requirePositive(totalAmount, "totalAmount");
        this.settledAmount = requireNonNegative(settledAmount, "settledAmount");
        this.outstandingAmount = requireNonNegative(outstandingAmount, "outstandingAmount");
        this.settledAt = settledAt;
        validateSettlementConsistency(this.status, this.paymentStatus, this.totalAmount, this.settledAmount,
                this.outstandingAmount, this.settledAt);
    }

    public static Invoice create(UUID tenantId, UUID organizationId, UUID customerThirdPartyId, UUID orderId,
            String invoiceNumber, List<InvoiceLine> lines, String currency) {
        Instant now = Instant.now();
        BigDecimal totalQuantity = lines.stream().map(InvoiceLine::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subtotalAmount = lines.stream().map(InvoiceLine::lineAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Invoice(UUID.randomUUID(), tenantId, now, now, organizationId, customerThirdPartyId, orderId,
                invoiceNumber, currency, STATUS_DRAFT, PAYMENT_STATUS_UNPAID, lines, totalQuantity, subtotalAmount,
                subtotalAmount, BigDecimal.ZERO, subtotalAmount, null);
    }

    public static Invoice rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID customerThirdPartyId, UUID orderId, String invoiceNumber, String currency, String status,
            String paymentStatus, List<InvoiceLine> lines, BigDecimal totalQuantity, BigDecimal subtotalAmount,
            BigDecimal totalAmount, BigDecimal settledAmount, BigDecimal outstandingAmount, Instant settledAt) {
        return new Invoice(id, tenantId, createdAt, updatedAt, organizationId, customerThirdPartyId, orderId,
                invoiceNumber, currency, status, paymentStatus, lines, totalQuantity, subtotalAmount, totalAmount,
                settledAmount, outstandingAmount, settledAt);
    }

    public Invoice post() {
        if (!STATUS_DRAFT.equals(status)) {
            throw new InvalidInvoiceStateException(id(), status, STATUS_DRAFT);
        }
        return new Invoice(id(), tenantId(), createdAt(), Instant.now(), organizationId, customerThirdPartyId, orderId,
                invoiceNumber, currency, STATUS_POSTED, paymentStatus, lines, totalQuantity, subtotalAmount,
                totalAmount, settledAmount, outstandingAmount, settledAt);
    }

    public Invoice update(UUID organizationId, UUID customerThirdPartyId, UUID orderId, String invoiceNumber,
            List<InvoiceLine> lines, String currency) {
        if (!STATUS_DRAFT.equals(status)) {
            throw new InvalidInvoiceStateException(id(), status, STATUS_DRAFT);
        }
        BigDecimal nextTotalQuantity = lines.stream().map(InvoiceLine::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal nextSubtotalAmount = lines.stream().map(InvoiceLine::lineAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Invoice(id(), tenantId(), createdAt(), Instant.now(), organizationId, customerThirdPartyId, orderId,
                invoiceNumber, currency, STATUS_DRAFT, PAYMENT_STATUS_UNPAID, lines, nextTotalQuantity,
                nextSubtotalAmount, nextSubtotalAmount, BigDecimal.ZERO, nextSubtotalAmount, null);
    }

    public Invoice applySettlement(String settlementNumber, BigDecimal amount) {
        requireText(settlementNumber, "settlementNumber");
        BigDecimal normalizedAmount = requirePositive(amount, "amount");
        if (!STATUS_POSTED.equals(status)) {
            throw new IllegalArgumentException("invoice must be POSTED before settlement can be applied");
        }
        if (outstandingAmount.compareTo(normalizedAmount) < 0) {
            throw new IllegalArgumentException("settlement amount exceeds invoice outstanding amount");
        }
        BigDecimal nextSettledAmount = settledAmount.add(normalizedAmount);
        BigDecimal nextOutstandingAmount = totalAmount.subtract(nextSettledAmount);
        String nextPaymentStatus = nextOutstandingAmount.signum() == 0
                ? PAYMENT_STATUS_PAID
                : PAYMENT_STATUS_PARTIALLY_PAID;
        return new Invoice(id(), tenantId(), createdAt(), Instant.now(), organizationId, customerThirdPartyId, orderId,
                invoiceNumber, currency, status, nextPaymentStatus, lines, totalQuantity, subtotalAmount, totalAmount,
                nextSettledAmount, nextOutstandingAmount, Instant.now());
    }

    public UUID organizationId() { return organizationId; }
    public UUID customerThirdPartyId() { return customerThirdPartyId; }
    public UUID orderId() { return orderId; }
    public UUID productId() { return lines.getFirst().productId(); }
    public String invoiceNumber() { return invoiceNumber; }
    public BigDecimal quantity() { return lines.getFirst().quantity(); }
    public BigDecimal unitPrice() { return lines.getFirst().unitPrice(); }
    public String currency() { return currency; }
    public String status() { return status; }
    public String paymentStatus() { return paymentStatus; }
    public List<InvoiceLine> lines() { return lines; }
    public BigDecimal totalQuantity() { return totalQuantity; }
    public BigDecimal subtotalAmount() { return subtotalAmount; }
    public BigDecimal totalAmount() { return totalAmount; }
    public BigDecimal settledAmount() { return settledAmount; }
    public BigDecimal outstandingAmount() { return outstandingAmount; }
    public Instant settledAt() { return settledAt; }

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

    private static BigDecimal requireNonNegative(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " is required");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(field + " must be non-negative");
        }
        return value;
    }

    private static String normalizePaymentStatus(String value) {
        String normalized = requireText(value, "paymentStatus").toUpperCase();
        if (!ALLOWED_PAYMENT_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("paymentStatus must be one of " + ALLOWED_PAYMENT_STATUSES);
        }
        return normalized;
    }

    private static List<InvoiceLine> normalizeLines(List<InvoiceLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("at least one invoice line is required");
        }
        return List.copyOf(lines);
    }

    private static void validateSettlementConsistency(String documentStatus, String paymentStatus, BigDecimal totalAmount,
            BigDecimal settledAmount, BigDecimal outstandingAmount, Instant settledAt) {
        if (settledAmount.add(outstandingAmount).compareTo(totalAmount) != 0) {
            throw new IllegalArgumentException("settledAmount plus outstandingAmount must equal totalAmount");
        }
        if (!STATUS_POSTED.equals(documentStatus) && settledAmount.signum() > 0) {
            throw new IllegalArgumentException("only POSTED invoices can carry settlements");
        }
        if (PAYMENT_STATUS_UNPAID.equals(paymentStatus) && settledAmount.signum() != 0) {
            throw new IllegalArgumentException("UNPAID invoices must not carry settledAmount");
        }
        if (PAYMENT_STATUS_PAID.equals(paymentStatus) && outstandingAmount.signum() != 0) {
            throw new IllegalArgumentException("PAID invoices must not carry outstandingAmount");
        }
        if (!PAYMENT_STATUS_UNPAID.equals(paymentStatus) && settledAt == null) {
            throw new IllegalArgumentException("settledAt is required when paymentStatus is not UNPAID");
        }
    }
}
