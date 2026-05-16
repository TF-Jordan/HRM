package yowyob.comops.api.resource.adapter.in.web;

import yowyob.comops.api.resource.domain.model.MaterialResource;
import java.util.UUID;

public record MaterialResourceResponse(
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

    public static MaterialResourceResponse from(MaterialResource materialResource) {
        return new MaterialResourceResponse(materialResource.id(), materialResource.tenantId(),
                materialResource.organizationId(), materialResource.agencyId(), materialResource.resourceCode(),
                materialResource.name(), materialResource.category(), materialResource.serialNumber(),
                materialResource.status(), materialResource.latitude(), materialResource.longitude(),
                materialResource.ipAddress(), materialResource.macAddress());
    }
}
