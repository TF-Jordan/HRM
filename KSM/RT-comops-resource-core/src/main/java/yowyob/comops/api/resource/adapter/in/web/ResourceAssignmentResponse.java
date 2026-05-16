package yowyob.comops.api.resource.adapter.in.web;

import yowyob.comops.api.resource.domain.model.ResourceAssignment;
import java.time.Instant;
import java.util.UUID;

public record ResourceAssignmentResponse(UUID id, UUID resourceId, String assigneeType, UUID assigneeId,
        Instant assignedAt, String status, Instant unassignedAt) {
    public static ResourceAssignmentResponse from(ResourceAssignment assignment) {
        return new ResourceAssignmentResponse(assignment.id(), assignment.resourceId(), assignment.assigneeType(),
                assignment.assigneeId(), assignment.assignedAt(), assignment.status(), assignment.unassignedAt());
    }
}
