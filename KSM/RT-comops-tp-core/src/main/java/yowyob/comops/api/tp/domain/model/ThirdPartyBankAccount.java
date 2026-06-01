package yowyob.comops.api.tp.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;

public final class ThirdPartyBankAccount extends BaseEntity {

    private final UUID thirdPartyId;
    private final String label;
    private final String bankName;
    private final String iban;
    private final String swiftBic;
    private final String currency;
    private final boolean primary;

    private ThirdPartyBankAccount(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID thirdPartyId,
            String label, String bankName, String iban, String swiftBic, String currency, boolean primary) {
        super(id, tenantId, createdAt, updatedAt);
        this.thirdPartyId = requireUuid(thirdPartyId, "thirdPartyId");
        this.label = requireText(label, "label");
        this.bankName = requireText(bankName, "bankName");
        this.iban = requireText(iban, "iban").toUpperCase(Locale.ROOT);
        this.swiftBic = normalizeOptional(swiftBic);
        this.currency = normalizeCurrency(currency);
        this.primary = primary;
    }

    public static ThirdPartyBankAccount create(UUID tenantId, UUID thirdPartyId, String label, String bankName,
            String iban, String swiftBic, String currency, boolean primary) {
        Instant now = Instant.now();
        return new ThirdPartyBankAccount(UUID.randomUUID(), tenantId, now, now, thirdPartyId, label, bankName, iban,
                swiftBic, currency, primary);
    }

    public static ThirdPartyBankAccount rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID thirdPartyId, String label, String bankName, String iban, String swiftBic, String currency,
            boolean primary) {
        return new ThirdPartyBankAccount(id, tenantId, createdAt, updatedAt, thirdPartyId, label, bankName, iban,
                swiftBic, currency, primary);
    }

    public UUID thirdPartyId() { return thirdPartyId; }
    public String label() { return label; }
    public String bankName() { return bankName; }
    public String iban() { return iban; }
    public String swiftBic() { return swiftBic; }
    public String currency() { return currency; }
    public boolean primary() { return primary; }

    public ThirdPartyBankAccount markPrimary() {
        return new ThirdPartyBankAccount(id(), tenantId(), createdAt(), Instant.now(), thirdPartyId, label, bankName,
                iban, swiftBic, currency, true);
    }

    public ThirdPartyBankAccount clearPrimary() {
        return new ThirdPartyBankAccount(id(), tenantId(), createdAt(), Instant.now(), thirdPartyId, label, bankName,
                iban, swiftBic, currency, false);
    }

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

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeCurrency(String value) {
        if (value == null || value.isBlank()) {
            return "XAF";
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        Currency.getInstance(normalized);
        return normalized;
    }
}
