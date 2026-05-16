package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;

public record CreatePhysicalSpaceCommand(
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        UUID parentSpaceId,
        String code,
        String name,
        String spaceType,
        String description,
        Integer levelNumber,
        Integer capacity,
        boolean active) {
}
