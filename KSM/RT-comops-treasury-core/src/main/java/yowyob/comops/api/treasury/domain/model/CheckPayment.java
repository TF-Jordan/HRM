package yowyob.comops.api.treasury.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import yowyob.comops.api.treasury.domain.InvalidCheckPaymentStateException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class CheckPayment extends BaseEntity {
    private static final String STATUS_ISSUED = "ISSUED";
    private static final String STATUS_CLEARED = "CLEARED";
    private static final Set<String> ALLOWED_STATUSES = Set.of(STATUS_ISSUED, STATUS_CLEARED);

    private final UUID organizationId;
    private final UUID bankAccountId;
    private final String checkNumber;
    private final BigDecimal amount;
    private final String beneficiary;
    private final String status;

    private CheckPayment(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId, UUID bankAccountId,
            String checkNumber, BigDecimal amount, String beneficiary, String status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.bankAccountId = requireUuid(bankAccountId, "bankAccountId");
        this.checkNumber = requireText(checkNumber, "checkNumber").toUpperCase();
        this.amount = requireAmount(amount, "amount");
        this.beneficiary = requireText(beneficiary, "beneficiary");
        this.status = normalizeStatus(status);
    }

    public static CheckPayment issue(UUID tenantId, UUID organizationId, UUID bankAccountId, String checkNumber,
            BigDecimal amount, String beneficiary) {
        Instant now = Instant.now();
        return new CheckPayment(UUID.randomUUID(), tenantId, now, now, organizationId, bankAccountId, checkNumber, amount,
                beneficiary, STATUS_ISSUED);
    }

    public static CheckPayment rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID bankAccountId, String checkNumber, BigDecimal amount, String beneficiary,
            String status) {
        return new CheckPayment(id, tenantId, createdAt, updatedAt, organizationId, bankAccountId, checkNumber, amount,
                beneficiary, status);
    }

    public CheckPayment clear() {
        if (!STATUS_ISSUED.equals(status)) {
            throw new InvalidCheckPaymentStateException(id(), status, STATUS_ISSUED);
        }
        return new CheckPayment(id(), tenantId(), createdAt(), Instant.now(), organizationId, bankAccountId,
                checkNumber, amount, beneficiary, STATUS_CLEARED);
    }

    public UUID organizationId() { return organizationId; }
    public UUID bankAccountId() { return bankAccountId; }
    public String checkNumber() { return checkNumber; }
    public BigDecimal amount() { return amount; }
    public String beneficiary() { return beneficiary; }
    public String status() { return status; }

    private static UUID requireUuid(UUID value, String field) { if (value == null) throw new IllegalArgumentException(field + " is required"); return value; }
    private static String requireText(String value, String field) { if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required"); return value.trim(); }
    private static BigDecimal requireAmount(BigDecimal value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " is required");
        if (value.signum() <= 0) throw new IllegalArgumentException(field + " must be positive");
        return value;
    }
    private static String normalizeStatus(String value) {
        String normalized = requireText(value, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("status must be one of " + ALLOWED_STATUSES);
        }
        return normalized;
    }
}
