package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Agency extends BaseEntity {

    private final UUID organizationId;
    private final AgencyGovernanceStatus governanceStatus;
    private final UUID governedByUserId;
    private final Instant governedAt;
    private final String governanceReason;
    private final String code;
    private final UUID ownerId;
    private final UUID managerId;
    private final String name;
    private final String location;
    private final String description;
    private final boolean transferable;
    private final boolean active;
    private final String logoUri;
    private final UUID logoId;
    private final String shortName;
    private final String longName;
    private final boolean isIndividualBusiness;
    private final boolean isHeadquarter;
    private final String country;
    private final String city;
    private final Double latitude;
    private final Double longitude;
    private final String openTime;
    private final String closeTime;
    private final String phone;
    private final String email;
    private final String whatsapp;
    private final String greetingMessage;
    private final BigDecimal averageRevenue;
    private final BigDecimal capitalShare;
    private final String registrationNumber;
    private final String socialNetwork;
    private final String taxNumber;
    private final Set<String> keywords;
    private final boolean isPublic;
    private final boolean isBusiness;
    private final Integer totalAffiliatedCustomers;
    private final Instant deletedAt;
    private final String agencyType;

    private Agency(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            AgencyGovernanceStatus governanceStatus, UUID governedByUserId, Instant governedAt, String governanceReason,
            String code, UUID ownerId, UUID managerId, String name, String location, String description,
            boolean transferable, boolean active, String logoUri, UUID logoId, String shortName, String longName,
            boolean isIndividualBusiness, boolean isHeadquarter, String country, String city, Double latitude,
            Double longitude, String openTime, String closeTime, String phone, String email, String whatsapp,
            String greetingMessage, BigDecimal averageRevenue, BigDecimal capitalShare, String registrationNumber,
            String socialNetwork, String taxNumber, Set<String> keywords, boolean isPublic, boolean isBusiness,
            Integer totalAffiliatedCustomers, Instant deletedAt, String agencyType) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.governanceStatus = governanceStatus == null ? AgencyGovernanceStatus.ACTIVE : governanceStatus;
        this.governedByUserId = governedByUserId;
        this.governedAt = governedAt;
        this.governanceReason = normalizeOptional(governanceReason);
        this.code = normalizeCode(code);
        this.ownerId = ownerId;
        this.managerId = managerId;
        this.name = requireText(name, "name");
        this.location = normalizeOptional(location);
        this.description = normalizeOptional(description);
        this.transferable = transferable;
        this.active = active;
        this.logoUri = normalizeOptional(logoUri);
        this.logoId = logoId;
        this.shortName = normalizeOptional(shortName) == null ? this.name : shortName.trim();
        this.longName = normalizeOptional(longName) == null ? this.name : longName.trim();
        this.isIndividualBusiness = isIndividualBusiness;
        this.isHeadquarter = isHeadquarter;
        this.country = normalizeOptional(country);
        this.city = normalizeOptional(city);
        this.latitude = latitude;
        this.longitude = longitude;
        this.openTime = normalizeOptional(openTime);
        this.closeTime = normalizeOptional(closeTime);
        this.phone = normalizeOptional(phone);
        this.email = normalizeOptional(email);
        this.whatsapp = normalizeOptional(whatsapp);
        this.greetingMessage = normalizeOptional(greetingMessage);
        this.averageRevenue = averageRevenue;
        this.capitalShare = capitalShare;
        this.registrationNumber = normalizeOptional(registrationNumber);
        this.socialNetwork = normalizeOptional(socialNetwork);
        this.taxNumber = normalizeOptional(taxNumber);
        this.keywords = normalizeKeywords(keywords);
        this.isPublic = isPublic;
        this.isBusiness = isBusiness;
        this.totalAffiliatedCustomers = totalAffiliatedCustomers;
        this.deletedAt = deletedAt;
        this.agencyType = normalizeType(agencyType);
    }

    public static Agency create(UUID tenantId, UUID organizationId, String code, String name, String agencyType) {
        Instant now = Instant.now();
        return new Agency(UUID.randomUUID(), tenantId, now, now, organizationId, AgencyGovernanceStatus.ACTIVE, null,
                null, null, code, null, null, name, null, null, false, true, null, null, name, name, false, false,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, Set.of(),
                false, false, null, null, agencyType);
    }

    public static Agency create(UUID tenantId, UUID organizationId, String code, UUID ownerId, UUID managerId,
            String name, String location, String description, boolean transferable, boolean active, String logoUri,
            UUID logoId, String shortName, String longName, boolean isIndividualBusiness, boolean isHeadquarter,
            String country, String city, Double latitude, Double longitude, String openTime, String closeTime,
            String phone, String email, String whatsapp, String greetingMessage, BigDecimal averageRevenue,
            BigDecimal capitalShare, String registrationNumber, String socialNetwork, String taxNumber,
            Set<String> keywords, boolean isPublic, boolean isBusiness, Integer totalAffiliatedCustomers,
            String agencyType) {
        Instant now = Instant.now();
        return new Agency(UUID.randomUUID(), tenantId, now, now, organizationId, AgencyGovernanceStatus.ACTIVE, null,
                null, null, code, ownerId, managerId, name, location, description, transferable, active, logoUri,
                logoId, shortName, longName, isIndividualBusiness, isHeadquarter, country, city, latitude,
                longitude, openTime, closeTime, phone, email, whatsapp, greetingMessage, averageRevenue,
                capitalShare, registrationNumber, socialNetwork, taxNumber, keywords, isPublic, isBusiness,
                totalAffiliatedCustomers, null, agencyType);
    }

    public static Agency rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            String governanceStatus, UUID governedByUserId, Instant governedAt, String governanceReason,
            String code, String name, String agencyType, boolean active) {
        return new Agency(id, tenantId, createdAt, updatedAt, organizationId,
                AgencyGovernanceStatus.from(governanceStatus), governedByUserId, governedAt, governanceReason, code,
                null, null, name, null, null, false, active, null, null, name, name, false, false, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, Set.of(), false, false,
                null, null, agencyType);
    }

    public static Agency rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            String governanceStatus, UUID governedByUserId, Instant governedAt, String governanceReason,
            String code, UUID ownerId, UUID managerId, String name, String location, String description,
            boolean transferable, boolean active, String logoUri, UUID logoId, String shortName, String longName,
            boolean isIndividualBusiness, boolean isHeadquarter, String country, String city, Double latitude,
            Double longitude, String openTime, String closeTime, String phone, String email, String whatsapp,
            String greetingMessage, BigDecimal averageRevenue, BigDecimal capitalShare, String registrationNumber,
            String socialNetwork, String taxNumber, Set<String> keywords, boolean isPublic, boolean isBusiness,
            Integer totalAffiliatedCustomers, Instant deletedAt, String agencyType) {
        return new Agency(id, tenantId, createdAt, updatedAt, organizationId,
                AgencyGovernanceStatus.from(governanceStatus), governedByUserId, governedAt, governanceReason, code,
                ownerId, managerId, name, location, description, transferable, active, logoUri, logoId, shortName,
                longName, isIndividualBusiness, isHeadquarter, country, city, latitude, longitude, openTime,
                closeTime, phone, email, whatsapp, greetingMessage, averageRevenue, capitalShare,
                registrationNumber, socialNetwork, taxNumber, keywords, isPublic, isBusiness,
                totalAffiliatedCustomers, deletedAt, agencyType);
    }

    public UUID organizationId() { return organizationId; }
    public AgencyGovernanceStatus governanceStatus() { return governanceStatus; }
    public UUID governedByUserId() { return governedByUserId; }
    public Instant governedAt() { return governedAt; }
    public String governanceReason() { return governanceReason; }
    public String code() { return code; }
    public UUID ownerId() { return ownerId; }
    public UUID managerId() { return managerId; }
    public String name() { return name; }
    public String location() { return location; }
    public String description() { return description; }
    public boolean transferable() { return transferable; }
    public boolean active() { return active; }
    public String logoUri() { return logoUri; }
    public UUID logoId() { return logoId; }
    public String shortName() { return shortName; }
    public String longName() { return longName; }
    public boolean isIndividualBusiness() { return isIndividualBusiness; }
    public boolean isHeadquarter() { return isHeadquarter; }
    public String country() { return country; }
    public String city() { return city; }
    public Double latitude() { return latitude; }
    public Double longitude() { return longitude; }
    public String openTime() { return openTime; }
    public String closeTime() { return closeTime; }
    public String phone() { return phone; }
    public String email() { return email; }
    public String whatsapp() { return whatsapp; }
    public String greetingMessage() { return greetingMessage; }
    public BigDecimal averageRevenue() { return averageRevenue; }
    public BigDecimal capitalShare() { return capitalShare; }
    public String registrationNumber() { return registrationNumber; }
    public String socialNetwork() { return socialNetwork; }
    public String taxNumber() { return taxNumber; }
    public Set<String> keywords() { return keywords; }
    public boolean isPublic() { return isPublic; }
    public boolean isBusiness() { return isBusiness; }
    public Integer totalAffiliatedCustomers() { return totalAffiliatedCustomers; }
    public Instant deletedAt() { return deletedAt; }
    public String agencyType() { return agencyType; }

    public Agency update(String code, String name, String agencyType) {
        return update(code, ownerId, managerId, name, location, description, transferable, active, logoUri, logoId,
                shortName, longName, isIndividualBusiness, isHeadquarter, country, city, latitude, longitude,
                openTime, closeTime, phone, email, whatsapp, greetingMessage, averageRevenue, capitalShare,
                registrationNumber, socialNetwork, taxNumber, keywords, isPublic, isBusiness,
                totalAffiliatedCustomers, agencyType);
    }

    public Agency update(String code, UUID ownerId, UUID managerId, String name, String location, String description,
            boolean transferable, boolean active, String logoUri, UUID logoId, String shortName, String longName,
            boolean isIndividualBusiness, boolean isHeadquarter, String country, String city, Double latitude,
            Double longitude, String openTime, String closeTime, String phone, String email, String whatsapp,
            String greetingMessage, BigDecimal averageRevenue, BigDecimal capitalShare, String registrationNumber,
            String socialNetwork, String taxNumber, Set<String> keywords, boolean isPublic, boolean isBusiness,
            Integer totalAffiliatedCustomers, String agencyType) {
        return new Agency(id(), tenantId(), createdAt(), Instant.now(), organizationId, governanceStatus,
                governedByUserId, governedAt, governanceReason, code, ownerId, managerId, name, location,
                description, transferable, active, logoUri, logoId, shortName, longName, isIndividualBusiness,
                isHeadquarter, country, city, latitude, longitude, openTime, closeTime, phone, email, whatsapp,
                greetingMessage, averageRevenue, capitalShare, registrationNumber, socialNetwork, taxNumber, keywords,
                isPublic, isBusiness, totalAffiliatedCustomers, deletedAt, agencyType);
    }

    public Agency deactivate() {
        return new Agency(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                AgencyGovernanceStatus.SUSPENDED, governedByUserId, Instant.now(), "deactivated", code, ownerId,
                managerId, name, location, description, transferable, false, logoUri, logoId, shortName, longName,
                isIndividualBusiness, isHeadquarter, country, city, latitude, longitude, openTime, closeTime, phone,
                email, whatsapp, greetingMessage, averageRevenue, capitalShare, registrationNumber, socialNetwork,
                taxNumber, keywords, isPublic, isBusiness, totalAffiliatedCustomers, deletedAt, agencyType);
    }

    public Agency activate(UUID adminUserId, String reason) {
        return new Agency(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                AgencyGovernanceStatus.ACTIVE, adminUserId, Instant.now(), reason, code, ownerId, managerId, name,
                location, description, transferable, true, logoUri, logoId, shortName, longName,
                isIndividualBusiness, isHeadquarter, country, city, latitude, longitude, openTime, closeTime, phone,
                email, whatsapp, greetingMessage, averageRevenue, capitalShare, registrationNumber, socialNetwork,
                taxNumber, keywords, isPublic, isBusiness, totalAffiliatedCustomers, deletedAt, agencyType);
    }

    public Agency suspend(UUID adminUserId, String reason) {
        if (governanceStatus == AgencyGovernanceStatus.CLOSED) {
            throw new IllegalStateException("closed agency cannot be suspended");
        }
        return new Agency(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                AgencyGovernanceStatus.SUSPENDED, adminUserId, Instant.now(), reason, code, ownerId, managerId, name,
                location, description, transferable, false, logoUri, logoId, shortName, longName,
                isIndividualBusiness, isHeadquarter, country, city, latitude, longitude, openTime, closeTime, phone,
                email, whatsapp, greetingMessage, averageRevenue, capitalShare, registrationNumber, socialNetwork,
                taxNumber, keywords, isPublic, isBusiness, totalAffiliatedCustomers, deletedAt, agencyType);
    }

    public Agency close(UUID adminUserId, String reason) {
        return new Agency(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                AgencyGovernanceStatus.CLOSED, adminUserId, Instant.now(), reason, code, ownerId, managerId, name,
                location, description, transferable, false, logoUri, logoId, shortName, longName,
                isIndividualBusiness, isHeadquarter, country, city, latitude, longitude, openTime, closeTime, phone,
                email, whatsapp, greetingMessage, averageRevenue, capitalShare, registrationNumber, socialNetwork,
                taxNumber, keywords, isPublic, isBusiness, totalAffiliatedCustomers, deletedAt, agencyType);
    }

    private static String normalizeCode(String value) {
        return requireText(value, "code").toUpperCase(Locale.ROOT);
    }

    private static String normalizeType(String value) {
        String normalized = normalizeOptional(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static Set<String> normalizeKeywords(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        values.stream().filter(Objects::nonNull).map(String::trim).filter(value -> !value.isEmpty())
                .forEach(normalized::add);
        return Set.copyOf(normalized);
    }
}
