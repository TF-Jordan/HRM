package yowyob.comops.api.tp.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import yowyob.comops.api.common.domain.model.PartyRef;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ThirdParty extends BaseEntity {

    // --- existing core fields ---
    private final UUID organizationId;
    private final PartyRef partyRef;
    private final String code;
    private final Set<String> roles;
    private final boolean prospect;
    private final String segment;
    private final Integer qualificationScore;
    private final Instant lastContactedAt;
    private final Instant nextFollowUpAt;
    private final String followUpStatus;
    private final boolean active;
    private final Instant convertedAt;

    // --- canonical fields (all optional) ---
    private final String type;
    private final String legalForm;
    private final String uniqueIdentificationNumber;
    private final String tradeRegistrationNumber;
    private final String name;
    private final String acronym;
    private final String longName;
    private final String logoUri;
    private final UUID logoId;
    private final List<String> accountingAccountNumbers;
    private final List<String> authorizedPaymentMethods;
    private final BigDecimal authorizedCreditLimit;
    private final BigDecimal maxDiscountRate;
    private final boolean vatSubject;
    private final BigDecimal operationsBalance;
    private final BigDecimal openingBalance;
    private final Integer payTermNumber;
    private final String payTermType;
    private final String thirdPartyFamily;
    private final String classification;
    private final String taxNumber;
    private final int loyaltyPoints;
    private final int loyaltyPointsUsed;
    private final int loyaltyPointsExpired;
    private final Instant deletedAt;

    private ThirdParty(
            UUID id,
            UUID tenantId,
            Instant createdAt,
            Instant updatedAt,
            UUID organizationId,
            PartyRef partyRef,
            String referenceCode,
            String displayName,
            Set<String> roles,
            boolean prospect,
            String accountingAccount,
            String segment,
            Integer qualificationScore,
            Instant lastContactedAt,
            Instant nextFollowUpAt,
            String followUpStatus,
            boolean active,
            Instant convertedAt,
            // canonical fields
            String type,
            String legalForm,
            String uniqueIdentificationNumber,
            String tradeRegistrationNumber,
            String name,
            String acronym,
            String longName,
            String logoUri,
            UUID logoId,
            List<String> accountingAccountNumbers,
            List<String> authorizedPaymentMethods,
            BigDecimal authorizedCreditLimit,
            BigDecimal maxDiscountRate,
            boolean vatSubject,
            BigDecimal operationsBalance,
            BigDecimal openingBalance,
            Integer payTermNumber,
            String payTermType,
            String thirdPartyFamily,
            String classification,
            String taxNumber,
            int loyaltyPoints,
            int loyaltyPointsUsed,
            int loyaltyPointsExpired,
            Instant deletedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.partyRef = Objects.requireNonNull(partyRef, "partyRef is required");
        this.code = requireText(referenceCode, "referenceCode").toUpperCase();
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("roles are required");
        }
        this.roles = normalizeRoles(roles, prospect);
        this.prospect = prospect;
        this.segment = normalizeOptional(segment);
        this.qualificationScore = normalizeScore(qualificationScore);
        this.lastContactedAt = lastContactedAt;
        this.nextFollowUpAt = nextFollowUpAt;
        this.followUpStatus = normalizeFollowUpStatus(followUpStatus, nextFollowUpAt, lastContactedAt);
        this.active = active;
        this.convertedAt = convertedAt;
        // canonical
        this.type = firstNonBlank(normalizeOptional(type), deriveType(roles, prospect));
        this.legalForm = normalizeOptional(legalForm);
        this.uniqueIdentificationNumber = normalizeOptional(uniqueIdentificationNumber);
        this.tradeRegistrationNumber = normalizeOptional(tradeRegistrationNumber);
        this.name = requireText(firstNonBlank(name, displayName), "name");
        this.acronym = acronym == null || acronym.isBlank() ? null : acronym.trim();
        this.longName = longName == null || longName.isBlank() ? this.name : longName.trim();
        this.logoUri = logoUri == null || logoUri.isBlank() ? null : logoUri.trim();
        this.logoId = logoId;
        this.accountingAccountNumbers = normalizeAccountingAccountNumbers(accountingAccountNumbers, accountingAccount);
        this.authorizedPaymentMethods = authorizedPaymentMethods == null ? List.of() : List.copyOf(authorizedPaymentMethods);
        this.authorizedCreditLimit = authorizedCreditLimit;
        this.maxDiscountRate = maxDiscountRate;
        this.vatSubject = vatSubject;
        this.operationsBalance = operationsBalance;
        this.openingBalance = openingBalance;
        this.payTermNumber = payTermNumber;
        this.payTermType = normalizeOptional(payTermType);
        this.thirdPartyFamily = firstNonBlank(normalizeOptional(thirdPartyFamily), deriveThirdPartyFamily(roles, prospect));
        this.classification = normalizeOptional(classification);
        this.taxNumber = taxNumber == null || taxNumber.isBlank() ? null : taxNumber.trim();
        this.loyaltyPoints = loyaltyPoints;
        this.loyaltyPointsUsed = loyaltyPointsUsed;
        this.loyaltyPointsExpired = loyaltyPointsExpired;
        this.deletedAt = deletedAt;
    }

    /** Minimal create — existing callers unaffected. */
    public static ThirdParty create(UUID tenantId, UUID organizationId, PartyRef partyRef, String referenceCode,
            String displayName, Set<String> roles, boolean prospect, String accountingAccount, String segment,
            Integer qualificationScore, boolean active) {
        Instant now = Instant.now();
        return new ThirdParty(UUID.randomUUID(), tenantId, now, now, organizationId, partyRef, referenceCode,
                displayName, roles, prospect, accountingAccount, segment, qualificationScore, null, null, null,
                active, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, false, null, null, null, null, null, null, null,
                0, 0, 0, null);
    }

    /** Full create — all canonical fields. */
    public static ThirdParty create(UUID tenantId, UUID organizationId, PartyRef partyRef, String referenceCode,
            String displayName, Set<String> roles, boolean prospect, String accountingAccount, String segment,
            Integer qualificationScore, boolean active,
            String type, String legalForm, String uniqueIdentificationNumber, String tradeRegistrationNumber,
            String name, String acronym, String longName, String logoUri, UUID logoId,
            List<String> accountingAccountNumbers, List<String> authorizedPaymentMethods,
            BigDecimal authorizedCreditLimit, BigDecimal maxDiscountRate, boolean vatSubject,
            BigDecimal operationsBalance, BigDecimal openingBalance, Integer payTermNumber, String payTermType,
            String thirdPartyFamily, String classification, String taxNumber) {
        Instant now = Instant.now();
        return new ThirdParty(UUID.randomUUID(), tenantId, now, now, organizationId, partyRef, referenceCode,
                displayName, roles, prospect, accountingAccount, segment, qualificationScore, null, null, null,
                active, null,
                type, legalForm, uniqueIdentificationNumber, tradeRegistrationNumber, name, acronym, longName,
                logoUri, logoId, accountingAccountNumbers, authorizedPaymentMethods, authorizedCreditLimit,
                maxDiscountRate, vatSubject, operationsBalance, openingBalance, payTermNumber, payTermType,
                thirdPartyFamily, classification, taxNumber, 0, 0, 0, null);
    }

    /** Minimal rehydrate — existing callers unaffected. */
    public static ThirdParty rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, PartyRef partyRef, String referenceCode, String displayName, Set<String> roles,
            boolean prospect, String accountingAccount, String segment, Integer qualificationScore, boolean active,
            Instant lastContactedAt, Instant nextFollowUpAt, String followUpStatus, Instant convertedAt) {
        return new ThirdParty(id, tenantId, createdAt, updatedAt, organizationId, partyRef, referenceCode,
                displayName, roles, prospect, accountingAccount, segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, followUpStatus, active, convertedAt,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, false, null, null, null, null, null, null, null,
                0, 0, 0, null);
    }

    /** Full rehydrate — all canonical fields. */
    public static ThirdParty rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, PartyRef partyRef, String referenceCode, String displayName, Set<String> roles,
            boolean prospect, String accountingAccount, String segment, Integer qualificationScore, boolean active,
            Instant lastContactedAt, Instant nextFollowUpAt, String followUpStatus, Instant convertedAt,
            String type, String legalForm, String uniqueIdentificationNumber, String tradeRegistrationNumber,
            String name, String acronym, String longName, String logoUri, UUID logoId,
            List<String> accountingAccountNumbers, List<String> authorizedPaymentMethods,
            BigDecimal authorizedCreditLimit, BigDecimal maxDiscountRate, boolean vatSubject,
            BigDecimal operationsBalance, BigDecimal openingBalance, Integer payTermNumber, String payTermType,
            String thirdPartyFamily, String classification, String taxNumber,
            int loyaltyPoints, int loyaltyPointsUsed, int loyaltyPointsExpired, Instant deletedAt) {
        return new ThirdParty(id, tenantId, createdAt, updatedAt, organizationId, partyRef, referenceCode,
                displayName, roles, prospect, accountingAccount, segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, followUpStatus, active, convertedAt,
                type, legalForm, uniqueIdentificationNumber, tradeRegistrationNumber, name, acronym, longName,
                logoUri, logoId, accountingAccountNumbers, authorizedPaymentMethods, authorizedCreditLimit,
                maxDiscountRate, vatSubject, operationsBalance, openingBalance, payTermNumber, payTermType,
                thirdPartyFamily, classification, taxNumber, loyaltyPoints, loyaltyPointsUsed, loyaltyPointsExpired, deletedAt);
    }

    // --- existing accessors ---
    public UUID organizationId() { return organizationId; }
    public PartyRef partyRef() { return partyRef; }
    public String code() { return code; }
    public String referenceCode() { return code; }
    public String displayName() { return name; }
    public Set<String> roles() { return roles; }
    public boolean prospect() { return prospect; }
    public String accountingAccount() { return accountingAccountNumbers.isEmpty() ? null : accountingAccountNumbers.getFirst(); }
    public String segment() { return segment; }
    public Integer qualificationScore() { return qualificationScore; }
    public Instant lastContactedAt() { return lastContactedAt; }
    public Instant nextFollowUpAt() { return nextFollowUpAt; }
    public String followUpStatus() { return followUpStatus; }
    public boolean active() { return active; }
    public Instant convertedAt() { return convertedAt; }
    public boolean hasRole(String role) { return roles.contains(requireText(role, "role").toUpperCase()); }

    // --- canonical accessors ---
    public String type() { return type; }
    public String legalForm() { return legalForm; }
    public String uniqueIdentificationNumber() { return uniqueIdentificationNumber; }
    public String tradeRegistrationNumber() { return tradeRegistrationNumber; }
    public String name() { return name; }
    public String acronym() { return acronym; }
    public String longName() { return longName; }
    public String logoUri() { return logoUri; }
    public UUID logoId() { return logoId; }
    public List<String> accountingAccountNumbers() { return accountingAccountNumbers; }
    public List<String> authorizedPaymentMethods() { return authorizedPaymentMethods; }
    public BigDecimal authorizedCreditLimit() { return authorizedCreditLimit; }
    public BigDecimal maxDiscountRate() { return maxDiscountRate; }
    public boolean vatSubject() { return vatSubject; }
    public BigDecimal operationsBalance() { return operationsBalance; }
    public BigDecimal openingBalance() { return openingBalance; }
    public Integer payTermNumber() { return payTermNumber; }
    public String payTermType() { return payTermType; }
    public String thirdPartyFamily() { return thirdPartyFamily; }
    public String classification() { return classification; }
    public String taxNumber() { return taxNumber; }
    public int loyaltyPoints() { return loyaltyPoints; }
    public int loyaltyPointsUsed() { return loyaltyPointsUsed; }
    public int loyaltyPointsExpired() { return loyaltyPointsExpired; }
    public Instant deletedAt() { return deletedAt; }
    public boolean enabled() { return active; }

    // --- existing mutation methods (preserved unchanged) ---
    public ThirdParty update(String referenceCode, String displayName, Set<String> roles, boolean prospect,
            String accountingAccount, String segment, Integer qualificationScore, boolean active) {
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, referenceCode,
                displayName, roles, prospect, accountingAccount, segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, followUpStatus, active, convertedAt,
                type, legalForm, uniqueIdentificationNumber, tradeRegistrationNumber, name, acronym, longName,
                logoUri, logoId, accountingAccountNumbers, authorizedPaymentMethods, authorizedCreditLimit,
                maxDiscountRate, vatSubject, operationsBalance, openingBalance, payTermNumber, payTermType,
                thirdPartyFamily, classification, taxNumber, loyaltyPoints, loyaltyPointsUsed, loyaltyPointsExpired, deletedAt);
    }

    public ThirdParty update(String referenceCode, String displayName, Set<String> roles, boolean prospect,
            String accountingAccount, String segment, Integer qualificationScore, boolean active,
            String type, String legalForm, String uniqueIdentificationNumber, String tradeRegistrationNumber,
            String name, String acronym, String longName, String logoUri, UUID logoId,
            List<String> accountingAccountNumbers, List<String> authorizedPaymentMethods,
            BigDecimal authorizedCreditLimit, BigDecimal maxDiscountRate, boolean vatSubject,
            BigDecimal operationsBalance, BigDecimal openingBalance, Integer payTermNumber, String payTermType,
            String thirdPartyFamily, String classification, String taxNumber) {
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, referenceCode,
                displayName, roles, prospect, accountingAccount, segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, followUpStatus, active, convertedAt,
                type, legalForm, uniqueIdentificationNumber, tradeRegistrationNumber, name, acronym, longName,
                logoUri, logoId, accountingAccountNumbers, authorizedPaymentMethods, authorizedCreditLimit,
                maxDiscountRate, vatSubject, operationsBalance, openingBalance, payTermNumber, payTermType,
                thirdPartyFamily, classification, taxNumber, loyaltyPoints, loyaltyPointsUsed, loyaltyPointsExpired, deletedAt);
    }

    public ThirdParty defineAccountingAccount(String accountingAccount) {
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, code(),
                displayName(), roles, prospect, accountingAccount, segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, followUpStatus, active, convertedAt,
                type, legalForm, uniqueIdentificationNumber, tradeRegistrationNumber, name, acronym, longName,
                logoUri, logoId, accountingAccountNumbers, authorizedPaymentMethods, authorizedCreditLimit,
                maxDiscountRate, vatSubject, operationsBalance, openingBalance, payTermNumber, payTermType,
                thirdPartyFamily, classification, taxNumber, loyaltyPoints, loyaltyPointsUsed, loyaltyPointsExpired, deletedAt);
    }

    public ThirdParty assignAccountingAccounts(List<String> accountingAccountNumbers) {
        List<String> normalized = normalizeAccountingAccountNumbers(accountingAccountNumbers, null);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("accountingAccountNumbers must not be empty");
        }
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, code(),
                displayName(), roles, prospect, normalized.getFirst(), segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, followUpStatus, active, convertedAt,
                type, legalForm, uniqueIdentificationNumber, tradeRegistrationNumber, name, acronym, longName,
                logoUri, logoId, normalized, authorizedPaymentMethods, authorizedCreditLimit,
                maxDiscountRate, vatSubject, operationsBalance, openingBalance, payTermNumber, payTermType,
                thirdPartyFamily, classification, taxNumber, loyaltyPoints, loyaltyPointsUsed, loyaltyPointsExpired, deletedAt);
    }

    public ThirdParty qualify(String segment, Integer qualificationScore) {
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, code(),
                displayName(), roles, prospect, accountingAccount(), segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, followUpStatus, active, convertedAt,
                type, legalForm, uniqueIdentificationNumber, tradeRegistrationNumber, name, acronym, longName,
                logoUri, logoId, accountingAccountNumbers, authorizedPaymentMethods, authorizedCreditLimit,
                maxDiscountRate, vatSubject, operationsBalance, openingBalance, payTermNumber, payTermType,
                thirdPartyFamily, classification, taxNumber, loyaltyPoints, loyaltyPointsUsed, loyaltyPointsExpired, deletedAt);
    }

    public ThirdParty scheduleFollowUp(Instant nextFollowUpAt) {
        if (nextFollowUpAt == null) {
            throw new IllegalArgumentException("nextFollowUpAt is required");
        }
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, code(),
                displayName(), roles, prospect, accountingAccount(), segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, "SCHEDULED", active, convertedAt,
                type, legalForm, uniqueIdentificationNumber, tradeRegistrationNumber, name, acronym, longName,
                logoUri, logoId, accountingAccountNumbers, authorizedPaymentMethods, authorizedCreditLimit,
                maxDiscountRate, vatSubject, operationsBalance, openingBalance, payTermNumber, payTermType,
                thirdPartyFamily, classification, taxNumber, loyaltyPoints, loyaltyPointsUsed, loyaltyPointsExpired, deletedAt);
    }

    public ThirdParty completeFollowUp(Instant contactedAt, Instant nextFollowUpAt) {
        Instant effectiveContactedAt = contactedAt == null ? Instant.now() : contactedAt;
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, code(),
                displayName(), roles, prospect, accountingAccount(), segment, qualificationScore, effectiveContactedAt,
                nextFollowUpAt, nextFollowUpAt == null ? "COMPLETED" : "SCHEDULED", active, convertedAt,
                type, legalForm, uniqueIdentificationNumber, tradeRegistrationNumber, name, acronym, longName,
                logoUri, logoId, accountingAccountNumbers, authorizedPaymentMethods, authorizedCreditLimit,
                maxDiscountRate, vatSubject, operationsBalance, openingBalance, payTermNumber, payTermType,
                thirdPartyFamily, classification, taxNumber, loyaltyPoints, loyaltyPointsUsed, loyaltyPointsExpired, deletedAt);
    }

    public ThirdParty activate() {
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, code(),
                displayName(), roles, prospect, accountingAccount(), segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, followUpStatus, true, convertedAt,
                type, legalForm, uniqueIdentificationNumber, tradeRegistrationNumber, name, acronym, longName,
                logoUri, logoId, accountingAccountNumbers, authorizedPaymentMethods, authorizedCreditLimit,
                maxDiscountRate, vatSubject, operationsBalance, openingBalance, payTermNumber, payTermType,
                thirdPartyFamily, classification, taxNumber, loyaltyPoints, loyaltyPointsUsed, loyaltyPointsExpired, deletedAt);
    }

    public ThirdParty deactivate() {
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, code(),
                displayName(), roles, prospect, accountingAccount(), segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, followUpStatus, false, convertedAt,
                type, legalForm, uniqueIdentificationNumber, tradeRegistrationNumber, name, acronym, longName,
                logoUri, logoId, accountingAccountNumbers, authorizedPaymentMethods, authorizedCreditLimit,
                maxDiscountRate, vatSubject, operationsBalance, openingBalance, payTermNumber, payTermType,
                thirdPartyFamily, classification, taxNumber, loyaltyPoints, loyaltyPointsUsed, loyaltyPointsExpired, deletedAt);
    }

    public ThirdParty convertToCustomer() {
        LinkedHashSet<String> convertedRoles = new LinkedHashSet<>(roles);
        convertedRoles.remove("PROSPECT");
        convertedRoles.add("CUSTOMER");
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, code(),
                displayName(), Set.copyOf(convertedRoles), false, accountingAccount(), segment, qualificationScore,
                lastContactedAt, nextFollowUpAt, followUpStatus, active,
                convertedAt == null ? Instant.now() : convertedAt,
                type, legalForm, uniqueIdentificationNumber, tradeRegistrationNumber, name, acronym, longName,
                logoUri, logoId, accountingAccountNumbers, authorizedPaymentMethods, authorizedCreditLimit,
                maxDiscountRate, vatSubject, operationsBalance, openingBalance, payTermNumber, payTermType,
                thirdPartyFamily, classification, taxNumber, loyaltyPoints, loyaltyPointsUsed, loyaltyPointsExpired, deletedAt);
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
        return value.trim().toUpperCase();
    }

    private static Integer normalizeScore(Integer value) {
        if (value == null) {
            return null;
        }
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("qualificationScore must be between 0 and 100");
        }
        return value;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String deriveType(Set<String> roles, boolean prospect) {
        if (prospect) {
            return "PROSPECT";
        }
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        return roles.iterator().next();
    }

    private static String deriveThirdPartyFamily(Set<String> roles, boolean prospect) {
        return deriveType(roles, prospect);
    }

    private static List<String> normalizeAccountingAccountNumbers(List<String> values, String accountingAccount) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (values != null) {
            values.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .forEach(normalized::add);
        }
        if (accountingAccount != null && !accountingAccount.isBlank()) {
            normalized.add(accountingAccount.trim());
        }
        return List.copyOf(normalized);
    }

    private static String normalizeFollowUpStatus(String value, Instant nextFollowUpAt, Instant lastContactedAt) {
        if (value == null || value.isBlank()) {
            if (nextFollowUpAt != null) {
                return "SCHEDULED";
            }
            if (lastContactedAt != null) {
                return "COMPLETED";
            }
            return null;
        }
        return value.trim().toUpperCase();
    }

    private static Set<String> normalizeRoles(Set<String> roles, boolean prospect) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        roles.stream()
                .map(role -> requireText(role, "role").toUpperCase())
                .forEach(normalized::add);
        if (prospect) {
            normalized.add("PROSPECT");
        }
        return Set.copyOf(normalized);
    }
}
