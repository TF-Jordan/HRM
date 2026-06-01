package yowyob.comops.api.resource.domain;

import java.util.UUID;

public final class ResourceAssignmentNotFoundException extends RuntimeException {

    public ResourceAssignmentNotFoundException(UUID resourceId) {
        super("Active resource assignment not found for resource: " + resourceId);
    }
}
