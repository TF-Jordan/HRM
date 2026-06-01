package yowyob.comops.api.treasury.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class BankStatement extends BaseEntity {

    private final UUID organizationId;
    private final UUID bankAccountId;
    private final String statementNumber;
    private final LocalDate statementDate;
    private final BigDecimal openingBalance;
    private final BigDecimal closingBalance;

    private BankStatement(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID bankAccountId, String statementNumber, LocalDate statementDate, BigDecimal openingBalance,
            BigDecimal closingBalance) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.bankAccountId = requireUuid(bankAccountId, "bankAccountId");
        this.statementNumber = requireText(statementNumber, "statementNumber").toUpperCase();
        this.statementDate = java.util.Objects.requireNonNull(statementDate, "statementDate is required");
        this.openingBalance = requireAmount(openingBalance, "openingBalance");
        this.closingBalance = requireAmount(closingBalance, "closingBalance");
    }

    public static BankStatement register(UUID tenantId, UUID organizationId, UUID bankAccountId, String statementNumber,
            LocalDate statementDate, BigDecimal openingBalance, BigDecimal closingBalance) {
        Instant now = Instant.now();
        return new BankStatement(UUID.randomUUID(), tenantId, now, now, organizationId, bankAccountId, statementNumber,
                statementDate, openingBalance, closingBalance);
    }

    public static BankStatement rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID bankAccountId, String statementNumber, LocalDate statementDate,
            BigDecimal openingBalance, BigDecimal closingBalance) {
        return new BankStatement(id, tenantId, createdAt, updatedAt, organizationId, bankAccountId, statementNumber,
                statementDate, openingBalance, closingBalance);
    }

    public UUID organizationId() { return organizationId; }
    public UUID bankAccountId() { return bankAccountId; }
    public String statementNumber() { return statementNumber; }
    public LocalDate statementDate() { return statementDate; }
    public BigDecimal openingBalance() { return openingBalance; }
    public BigDecimal closingBalance() { return closingBalance; }

    private static UUID requireUuid(UUID value, String field) { if (value == null) throw new IllegalArgumentException(field + " is required"); return value; }
    private static String requireText(String value, String field) { if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required"); return value.trim(); }
    private static BigDecimal requireAmount(BigDecimal value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " is required");
        return value;
    }
}
