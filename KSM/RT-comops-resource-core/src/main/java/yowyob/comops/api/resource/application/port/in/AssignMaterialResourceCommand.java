package yowyob.comops.api.resource.application.port.in;

import java.util.UUID;

public record AssignMaterialResourceCommand(UUID resourceId, String assigneeType, UUID assigneeId) {
}
