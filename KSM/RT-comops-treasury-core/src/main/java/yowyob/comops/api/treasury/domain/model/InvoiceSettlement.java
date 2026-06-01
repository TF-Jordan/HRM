package yowyob.comops.api.treasury.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class InvoiceSettlement extends BaseEntity {
    private static final String STATUS_REGISTERED = "REGISTERED";
    private static final Set<String> ALLOWED_STATUSES = Set.of(STATUS_REGISTERED);
    private static final Set<String> ALLOWED_METHODS = Set.of("BANK_TRANSFER", "CARD", "CASH", "CHECK");

    private final UUID organizationId;
    private final UUID bankAccountId;
    private final UUID invoiceId;
    private final String settlementNumber;
    private final String paymentMethod;
    private final BigDecimal amount;
    private final String currency;
    private final String status;

    private InvoiceSettlement(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID bankAccountId, UUID invoiceId, String settlementNumber, String paymentMethod, BigDecimal amount,
            String currency, String status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.bankAccountId = requireUuid(bankAccountId, "bankAccountId");
        this.invoiceId = requireUuid(invoiceId, "invoiceId");
        this.settlementNumber = requireText(settlementNumber, "settlementNumber").toUpperCase();
        this.paymentMethod = normalizePaymentMethod(paymentMethod);
        this.amount = requireAmount(amount, "amount");
        this.currency = requireText(currency, "currency").toUpperCase();
        this.status = normalizeStatus(status);
    }

    public static InvoiceSettlement register(UUID tenantId, UUID organizationId, UUID bankAccountId, UUID invoiceId,
            String settlementNumber, String paymentMethod, BigDecimal amount, String currency) {
        Instant now = Instant.now();
        return new InvoiceSettlement(UUID.randomUUID(), tenantId, now, now, organizationId, bankAccountId, invoiceId,
                settlementNumber, paymentMethod, amount, currency, STATUS_REGISTERED);
    }

    public static InvoiceSettlement rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID bankAccountId, UUID invoiceId, String settlementNumber, String paymentMethod,
            BigDecimal amount, String currency, String status) {
        return new InvoiceSettlement(id, tenantId, createdAt, updatedAt, organizationId, bankAccountId, invoiceId,
                settlementNumber, paymentMethod, amount, currency, status);
    }

    public UUID organizationId() { return organizationId; }
    public UUID bankAccountId() { return bankAccountId; }
    public UUID invoiceId() { return invoiceId; }
    public String settlementNumber() { return settlementNumber; }
    public String paymentMethod() { return paymentMethod; }
    public BigDecimal amount() { return amount; }
    public String currency() { return currency; }
    public String status() { return status; }

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

    private static BigDecimal requireAmount(BigDecimal value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }

    private static String normalizePaymentMethod(String value) {
        String normalized = requireText(value, "paymentMethod").toUpperCase();
        if (!ALLOWED_METHODS.contains(normalized)) {
            throw new IllegalArgumentException("paymentMethod must be one of " + ALLOWED_METHODS);
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
}
