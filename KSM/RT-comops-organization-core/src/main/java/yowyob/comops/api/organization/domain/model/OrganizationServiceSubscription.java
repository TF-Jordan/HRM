package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import yowyob.comops.api.common.domain.model.PlatformServiceCode;
import java.time.Instant;
import java.util.UUID;

public final class OrganizationServiceSubscription extends BaseEntity {

    private final UUID organizationId;
    private final String serviceCode;
    private final long requestQuotaLimit;
    private final long requestQuotaWindowSeconds;

    private OrganizationServiceSubscription(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, String serviceCode, long requestQuotaLimit, long requestQuotaWindowSeconds) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.serviceCode = PlatformServiceCode.from(serviceCode).code();
        this.requestQuotaLimit = requirePositive(requestQuotaLimit, "requestQuotaLimit");
        this.requestQuotaWindowSeconds = requirePositive(requestQuotaWindowSeconds, "requestQuotaWindowSeconds");
    }

    public static OrganizationServiceSubscription create(UUID tenantId, UUID organizationId, String serviceCode,
            long requestQuotaLimit, long requestQuotaWindowSeconds) {
        Instant now = Instant.now();
        return new OrganizationServiceSubscription(UUID.randomUUID(), tenantId, now, now, organizationId, serviceCode,
                requestQuotaLimit, requestQuotaWindowSeconds);
    }

    public static OrganizationServiceSubscription rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, String serviceCode, long requestQuotaLimit, long requestQuotaWindowSeconds) {
        return new OrganizationServiceSubscription(id, tenantId, createdAt, updatedAt, organizationId, serviceCode,
                requestQuotaLimit, requestQuotaWindowSeconds);
    }

    public UUID organizationId() {
        return organizationId;
    }

    public String serviceCode() {
        return serviceCode;
    }

    public long requestQuotaLimit() {
        return requestQuotaLimit;
    }

    public long requestQuotaWindowSeconds() {
        return requestQuotaWindowSeconds;
    }

    public OrganizationServiceSubscription updateQuota(long requestQuotaLimit, long requestQuotaWindowSeconds) {
        return new OrganizationServiceSubscription(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                serviceCode, requestQuotaLimit, requestQuotaWindowSeconds);
    }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static long requirePositive(long value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be > 0");
        }
        return value;
    }
}
