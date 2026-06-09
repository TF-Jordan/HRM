package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Organization extends BaseEntity {

    private final UUID businessActorId;
    private final OrganizationGovernanceStatus governanceStatus;
    private final UUID governedByUserId;
    private final Instant governedAt;
    private final String governanceReason;
    private final String code;
    private final String service;
    private final boolean isIndividualBusiness;
    private final String email;
    private final String shortName;
    private final String longName;
    private final String description;
    private final String logoUri;
    private final UUID logoId;
    private final String websiteUrl;
    private final String socialNetwork;
    private final String businessRegistrationNumber;
    private final String taxNumber;
    private final BigDecimal capitalShare;
    private final String ceoName;
    private final Integer yearFounded;
    private final Set<String> keywords;
    private final Integer numberOfEmployees;
    private final String legalForm;
    private final boolean isActive;
    private final String status;
    private final Instant deletedAt;
    // Payroll/HR extensions (Priority 3 — required for legal payslips and multi-country payroll).
    // Optional on read; defaulted to safe values on legacy rows that pre-date them.
    private final String countryCode;
    private final String cnpsEmployerNumber;
    private final BigDecimal atRiskRate;
    private final String address;
    private final String city;
    private final String postalCode;
    private final String phone;
    private final String conventionCollective;

    private Organization(
            UUID id,
            UUID tenantId,
            Instant createdAt,
            Instant updatedAt,
            UUID businessActorId,
            OrganizationGovernanceStatus governanceStatus,
            UUID governedByUserId,
            Instant governedAt,
            String governanceReason,
            String code,
            String service,
            boolean isIndividualBusiness,
            String email,
            String shortName,
            String longName,
            String description,
            String logoUri,
            UUID logoId,
            String websiteUrl,
            String socialNetwork,
            String businessRegistrationNumber,
            String taxNumber,
            BigDecimal capitalShare,
            String ceoName,
            Integer yearFounded,
            Set<String> keywords,
            Integer numberOfEmployees,
            String legalForm,
            boolean isActive,
            String status,
            Instant deletedAt,
            String countryCode,
            String cnpsEmployerNumber,
            BigDecimal atRiskRate,
            String address,
            String city,
            String postalCode,
            String phone,
            String conventionCollective) {
        super(id, tenantId, createdAt, updatedAt);
        this.businessActorId = Objects.requireNonNull(businessActorId, "businessActorId is required");
        this.governanceStatus = governanceStatus == null ? OrganizationGovernanceStatus.PENDING_APPROVAL : governanceStatus;
        this.governedByUserId = governedByUserId;
        this.governedAt = governedAt;
        this.governanceReason = normalizeOptional(governanceReason);
        this.code = normalizeCode(code);
        this.service = normalizeRequiredEnumLike(service, "service");
        this.isIndividualBusiness = isIndividualBusiness;
        this.email = normalizeEmail(email);
        this.shortName = requireText(shortName, "shortName");
        this.longName = requireText(longName, "longName");
        this.description = normalizeOptional(description);
        this.logoUri = normalizeOptional(logoUri);
        this.logoId = logoId;
        this.websiteUrl = normalizeOptional(websiteUrl);
        this.socialNetwork = normalizeOptional(socialNetwork);
        this.businessRegistrationNumber = normalizeOptional(businessRegistrationNumber);
        this.taxNumber = normalizeOptional(taxNumber);
        this.capitalShare = capitalShare;
        this.ceoName = normalizeOptional(ceoName);
        this.yearFounded = yearFounded;
        this.keywords = normalizeStringSet(keywords, false);
        this.numberOfEmployees = numberOfEmployees;
        this.legalForm = normalizeOptional(legalForm);
        this.isActive = isActive;
        this.status = normalizeStatus(status, this.governanceStatus, isActive);
        this.deletedAt = deletedAt;
        this.countryCode = normalizeOptional(countryCode);
        this.cnpsEmployerNumber = normalizeOptional(cnpsEmployerNumber);
        this.atRiskRate = atRiskRate;
        this.address = normalizeOptional(address);
        this.city = normalizeOptional(city);
        this.postalCode = normalizeOptional(postalCode);
        this.phone = normalizeOptional(phone);
        this.conventionCollective = normalizeOptional(conventionCollective);
    }

    public static Organization create(UUID tenantId, UUID businessActorId, String code, String legalName, String displayName,
            String organizationType) {
        return create(tenantId, businessActorId, code, organizationType, false, null, displayName, legalName, null,
                null, null, null, null, null, null, null, null, null, Set.of(), null, null, true, null);
    }

    public static Organization create(UUID tenantId, UUID businessActorId, String code, String service,
            boolean isIndividualBusiness, String email, String shortName, String longName, String description,
            String logoUri, UUID logoId, String websiteUrl, String socialNetwork,
            String businessRegistrationNumber, String taxNumber, BigDecimal capitalShare, String ceoName,
            Integer yearFounded, Set<String> keywords, Integer numberOfEmployees, String legalForm,
            boolean isActive, String status) {
        Instant now = Instant.now();
        return new Organization(UUID.randomUUID(), tenantId, now, now, businessActorId,
                OrganizationGovernanceStatus.PENDING_APPROVAL, null, null, null, code, service,
                isIndividualBusiness, email, shortName, longName, description, logoUri, logoId, websiteUrl,
                socialNetwork, businessRegistrationNumber, taxNumber, capitalShare, ceoName, yearFounded, keywords,
                numberOfEmployees, legalForm, isActive, status, null, null, null, null,
                null, null, null, null, null);
    }

    public static Organization rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID businessActorId, String governanceStatus, UUID governedByUserId, Instant governedAt,
            String governanceReason, String code, String legalName, String displayName, String organizationType) {
        return rehydrate(id, tenantId, createdAt, updatedAt, businessActorId, governanceStatus, governedByUserId,
                governedAt, governanceReason, code, organizationType, false, null, displayName, legalName, null,
                null, null, null, null, null, null, null, null, null, Set.of(), null, null, true, null, null);
    }

    public static Organization rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID businessActorId, String governanceStatus, UUID governedByUserId, Instant governedAt,
            String governanceReason, String code, String service, boolean isIndividualBusiness, String email,
            String shortName, String longName, String description, String logoUri, UUID logoId, String websiteUrl,
            String socialNetwork, String businessRegistrationNumber, String taxNumber, BigDecimal capitalShare,
            String ceoName, Integer yearFounded, Set<String> keywords, Integer numberOfEmployees, String legalForm,
            boolean isActive, String status, Instant deletedAt) {
        return rehydrate(id, tenantId, createdAt, updatedAt, businessActorId, governanceStatus, governedByUserId,
                governedAt, governanceReason, code, service, isIndividualBusiness, email, shortName, longName,
                description, logoUri, logoId, websiteUrl, socialNetwork, businessRegistrationNumber, taxNumber,
                capitalShare, ceoName, yearFounded, keywords, numberOfEmployees, legalForm, isActive, status,
                deletedAt, null, null, null, null, null, null, null, null);
    }

    /** Full rehydrate including the payroll extensions; used by the persistence adapter. */
    public static Organization rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID businessActorId, String governanceStatus, UUID governedByUserId, Instant governedAt,
            String governanceReason, String code, String service, boolean isIndividualBusiness, String email,
            String shortName, String longName, String description, String logoUri, UUID logoId, String websiteUrl,
            String socialNetwork, String businessRegistrationNumber, String taxNumber, BigDecimal capitalShare,
            String ceoName, Integer yearFounded, Set<String> keywords, Integer numberOfEmployees, String legalForm,
            boolean isActive, String status, Instant deletedAt,
            String countryCode, String cnpsEmployerNumber, BigDecimal atRiskRate,
            String address, String city, String postalCode, String phone, String conventionCollective) {
        return new Organization(id, tenantId, createdAt, updatedAt, businessActorId,
                OrganizationGovernanceStatus.from(governanceStatus), governedByUserId, governedAt, governanceReason,
                code, service, isIndividualBusiness, email, shortName, longName, description, logoUri, logoId,
                websiteUrl, socialNetwork, businessRegistrationNumber, taxNumber, capitalShare, ceoName,
                yearFounded, keywords, numberOfEmployees, legalForm, isActive, status, deletedAt,
                countryCode, cnpsEmployerNumber, atRiskRate, address, city, postalCode, phone, conventionCollective);
    }

    /** Returns a copy with the payroll extension fields set; preserves all other fields. */
    public Organization withPayrollExtensions(String countryCode, String cnpsEmployerNumber,
                                              BigDecimal atRiskRate) {
        return new Organization(id(), tenantId(), createdAt(), Instant.now(), businessActorId, governanceStatus,
                governedByUserId, governedAt, governanceReason, code, service, isIndividualBusiness, email,
                shortName, longName, description, logoUri, logoId, websiteUrl, socialNetwork,
                businessRegistrationNumber, taxNumber, capitalShare, ceoName, yearFounded, keywords,
                numberOfEmployees, legalForm, isActive, status, deletedAt,
                countryCode, cnpsEmployerNumber, atRiskRate, address, city, postalCode, phone, conventionCollective);
    }

    public Organization update(String code, String legalName, String displayName, String organizationType) {
        return update(code, organizationType, isIndividualBusiness, email, displayName, legalName, description,
                logoUri, logoId, websiteUrl, socialNetwork, businessRegistrationNumber, taxNumber, capitalShare,
                ceoName, yearFounded, keywords, numberOfEmployees, legalForm, isActive, status);
    }

    public Organization update(String code, String service, boolean isIndividualBusiness, String email,
            String shortName, String longName, String description, String logoUri, UUID logoId, String websiteUrl,
            String socialNetwork, String businessRegistrationNumber, String taxNumber, BigDecimal capitalShare,
            String ceoName, Integer yearFounded, Set<String> keywords, Integer numberOfEmployees, String legalForm,
            boolean isActive, String status) {
        return new Organization(id(), tenantId(), createdAt(), Instant.now(), businessActorId, governanceStatus,
                governedByUserId, governedAt, governanceReason, code, service, isIndividualBusiness, email,
                shortName, longName, description, logoUri, logoId, websiteUrl, socialNetwork,
                businessRegistrationNumber, taxNumber, capitalShare, ceoName, yearFounded, keywords,
                numberOfEmployees, legalForm, isActive, status, deletedAt,
                countryCode, cnpsEmployerNumber, atRiskRate, address, city, postalCode, phone, conventionCollective);
    }

    public Organization transferOwnership(UUID newBusinessActorId) {
        return new Organization(id(), tenantId(), createdAt(), Instant.now(), newBusinessActorId, governanceStatus,
                governedByUserId, governedAt, governanceReason, code, service, isIndividualBusiness, email,
                shortName, longName, description, logoUri, logoId, websiteUrl, socialNetwork,
                businessRegistrationNumber, taxNumber, capitalShare, ceoName, yearFounded, keywords,
                numberOfEmployees, legalForm, isActive, status, deletedAt,
                countryCode, cnpsEmployerNumber, atRiskRate, address, city, postalCode, phone, conventionCollective);
    }

    public Organization approve(UUID adminUserId, String reason) {
        return govern(OrganizationGovernanceStatus.APPROVED, adminUserId, reason);
    }

    public Organization reject(UUID adminUserId, String reason) {
        return govern(OrganizationGovernanceStatus.REJECTED, adminUserId, reason);
    }

    public Organization suspend(UUID adminUserId, String reason) {
        if (governanceStatus == OrganizationGovernanceStatus.CLOSED) {
            throw new IllegalStateException("closed organization cannot be suspended");
        }
        return govern(OrganizationGovernanceStatus.SUSPENDED, adminUserId, reason);
    }

    public Organization close(UUID adminUserId, String reason) {
        return govern(OrganizationGovernanceStatus.CLOSED, adminUserId, reason);
    }

    public Organization reopen(UUID adminUserId, String reason) {
        return govern(OrganizationGovernanceStatus.APPROVED, adminUserId, reason);
    }

    private Organization govern(OrganizationGovernanceStatus nextStatus, UUID adminUserId, String reason) {
        boolean active = nextStatus != OrganizationGovernanceStatus.CLOSED
                && nextStatus != OrganizationGovernanceStatus.SUSPENDED
                && nextStatus != OrganizationGovernanceStatus.REJECTED;
        return new Organization(id(), tenantId(), createdAt(), Instant.now(), businessActorId,
                nextStatus, adminUserId, Instant.now(), reason, code, service, isIndividualBusiness, email,
                shortName, longName, description, logoUri, logoId, websiteUrl, socialNetwork,
                businessRegistrationNumber, taxNumber, capitalShare, ceoName, yearFounded, keywords,
                numberOfEmployees, legalForm, active, nextStatus.name(), deletedAt,
                countryCode, cnpsEmployerNumber, atRiskRate, address, city, postalCode, phone, conventionCollective);
    }

    public UUID businessActorId() { return businessActorId; }
    public OrganizationGovernanceStatus governanceStatus() { return governanceStatus; }
    public UUID governedByUserId() { return governedByUserId; }
    public Instant governedAt() { return governedAt; }
    public String governanceReason() { return governanceReason; }
    public String code() { return code; }
    public String service() { return service; }
    public boolean isIndividualBusiness() { return isIndividualBusiness; }
    public String email() { return email; }
    public String shortName() { return shortName; }
    public String longName() { return longName; }
    public String description() { return description; }
    public String logoUri() { return logoUri; }
    public UUID logoId() { return logoId; }
    public String websiteUrl() { return websiteUrl; }
    public String socialNetwork() { return socialNetwork; }
    public String businessRegistrationNumber() { return businessRegistrationNumber; }
    public String taxNumber() { return taxNumber; }
    public BigDecimal capitalShare() { return capitalShare; }
    public String ceoName() { return ceoName; }
    public Integer yearFounded() { return yearFounded; }
    public Set<String> keywords() { return keywords; }
    public Integer numberOfEmployees() { return numberOfEmployees; }
    public String legalForm() { return legalForm; }
    public boolean isActive() { return isActive; }
    public String status() { return status; }
    public Instant deletedAt() { return deletedAt; }
    public String legalName() { return longName; }
    public String displayName() { return shortName; }
    public String organizationType() { return service; }
    public String countryCode() { return countryCode; }
    public String cnpsEmployerNumber() { return cnpsEmployerNumber; }
    public BigDecimal atRiskRate() { return atRiskRate; }
    public String address() { return address; }
    public String city() { return city; }
    public String postalCode() { return postalCode; }
    public String phone() { return phone; }
    public String conventionCollective() { return conventionCollective; }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalizeCode(String value) {
        return requireText(value, "code").toUpperCase(Locale.ROOT);
    }

    private static String normalizeRequiredEnumLike(String value, String field) {
        return requireText(value, field).toUpperCase(Locale.ROOT);
    }

    private static String normalizeEmail(String value) {
        String normalized = normalizeOptional(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static Set<String> normalizeStringSet(Set<String> values, boolean uppercase) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> uppercase ? value.toUpperCase(Locale.ROOT) : value)
                .forEach(normalized::add);
        return Set.copyOf(normalized);
    }

    private static String normalizeStatus(String status, OrganizationGovernanceStatus governanceStatus, boolean isActive) {
        String normalized = normalizeOptional(status);
        if (normalized != null) {
            return normalized.toUpperCase(Locale.ROOT);
        }
        if (!isActive) {
            return "INACTIVE";
        }
        return governanceStatus == null ? "PENDING_APPROVAL" : governanceStatus.name();
    }
}
