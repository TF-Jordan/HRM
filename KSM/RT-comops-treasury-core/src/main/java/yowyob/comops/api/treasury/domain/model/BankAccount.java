package yowyob.comops.api.treasury.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class BankAccount extends BaseEntity {
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final Set<String> ALLOWED_STATUSES = Set.of(STATUS_ACTIVE);

    private final UUID organizationId;
    private final UUID bankThirdPartyId;
    private final UUID ownerThirdPartyId;
    private final String bankName;
    private final String accountNumber;
    private final String iban;
    private final String currency;
    private final String status;

    private BankAccount(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID bankThirdPartyId, UUID ownerThirdPartyId, String bankName, String accountNumber, String iban,
            String currency, String status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.bankThirdPartyId = bankThirdPartyId;
        this.ownerThirdPartyId = ownerThirdPartyId;
        this.bankName = requireText(bankName, "bankName");
        this.accountNumber = requireText(accountNumber, "accountNumber").toUpperCase();
        this.iban = requireText(iban, "iban").toUpperCase();
        this.currency = requireText(currency, "currency").toUpperCase();
        this.status = normalizeStatus(status);
    }

    public static BankAccount register(UUID tenantId, UUID organizationId, UUID bankThirdPartyId, String bankName,
            String accountNumber, String iban, String currency) {
        Instant now = Instant.now();
        return new BankAccount(UUID.randomUUID(), tenantId, now, now, organizationId, bankThirdPartyId, null, bankName,
                accountNumber, iban, currency, STATUS_ACTIVE);
    }

    public static BankAccount provision(UUID tenantId, UUID organizationId, UUID ownerThirdPartyId, String purpose,
            String currency) {
        Instant now = Instant.now();
        return new BankAccount(UUID.randomUUID(), tenantId, now, now, organizationId, null,
                Objects.requireNonNull(ownerThirdPartyId, "ownerThirdPartyId is required"),
                "KSM TREASURY", generateAccountNumber(ownerThirdPartyId, purpose),
                generateIban(organizationId, ownerThirdPartyId), normalizeCurrency(currency), STATUS_ACTIVE);
    }

    public static BankAccount rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID bankThirdPartyId, UUID ownerThirdPartyId, String bankName, String accountNumber, String iban,
            String currency, String status) {
        return new BankAccount(id, tenantId, createdAt, updatedAt, organizationId, bankThirdPartyId, ownerThirdPartyId,
                bankName, accountNumber, iban, currency, status);
    }

    public UUID organizationId() { return organizationId; }
    public UUID bankThirdPartyId() { return bankThirdPartyId; }
    public UUID ownerThirdPartyId() { return ownerThirdPartyId; }
    public String bankName() { return bankName; }
    public String accountNumber() { return accountNumber; }
    public String iban() { return iban; }
    public String currency() { return currency; }
    public String status() { return status; }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalizeStatus(String value) {
        String normalized = requireText(value, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("status must be one of " + ALLOWED_STATUSES);
        }
        return normalized;
    }

    private static String normalizeCurrency(String value) {
        if (value == null || value.isBlank()) {
            return "XAF";
        }
        return value.trim().toUpperCase();
    }

    private static String generateAccountNumber(UUID ownerThirdPartyId, String purpose) {
        String prefix = switch (purpose == null ? "" : purpose.trim().toUpperCase()) {
            case "CUSTOMER" -> "CUST";
            case "SUPPLIER" -> "SUPP";
            case "EMPLOYEE", "BENEFICIARY", "PAYEE" -> "PAYE";
            default -> "GENR";
        };
        String token = ownerThirdPartyId.toString().replace("-", "").toUpperCase();
        return prefix + "-" + token.substring(0, 16);
    }

    private static String generateIban(UUID organizationId, UUID ownerThirdPartyId) {
        String organizationToken = organizationId.toString().replace("-", "").toUpperCase();
        String ownerToken = ownerThirdPartyId.toString().replace("-", "").toUpperCase();
        return "KSM" + organizationToken.substring(0, 10) + ownerToken.substring(0, 18);
    }
}
