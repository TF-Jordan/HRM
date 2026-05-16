package yowyob.comops.api.resource.application.port.in;

import java.util.UUID;

public record RegisterMaterialResourceCommand(
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        String resourceCode,
        String name,
        String category,
        String serialNumber,
        Double latitude,
        Double longitude,
        String ipAddress,
        String macAddress) {
}
