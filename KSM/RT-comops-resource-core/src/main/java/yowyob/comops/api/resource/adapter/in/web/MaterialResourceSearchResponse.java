package yowyob.comops.api.resource.adapter.in.web;

import yowyob.comops.api.resource.domain.model.MaterialResourceSearchResult;
import java.util.UUID;

public record MaterialResourceSearchResponse(
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

    public static MaterialResourceSearchResponse from(MaterialResourceSearchResult result) {
        return new MaterialResourceSearchResponse(result.id(), result.tenantId(), result.organizationId(),
                result.agencyId(), result.resourceCode(), result.name(), result.category(), result.serialNumber(),
                result.status(), result.latitude(), result.longitude(), result.ipAddress(), result.macAddress());
    }
}
