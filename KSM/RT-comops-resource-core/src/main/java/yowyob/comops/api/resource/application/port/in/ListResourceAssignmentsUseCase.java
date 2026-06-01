package yowyob.comops.api.resource.application.port.in;

import yowyob.comops.api.resource.domain.model.ResourceAssignment;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListResourceAssignmentsUseCase {
    Flux<ResourceAssignment> listAssignments(UUID tenantId, UUID resourceId);
    Flux<ResourceAssignment> listAssignments(UUID tenantId, String assigneeType, UUID assigneeId);
}
