package yowyob.comops.api.resource.domain.model;

import java.util.UUID;

public record MaterialResourceSearchResult(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        String resourceCode,
        String name,
        String category,
        String serialNumber,
        String status,
        Double latitude,
        Double longitude,
        String ipAddress,
        String macAddress) {
}
