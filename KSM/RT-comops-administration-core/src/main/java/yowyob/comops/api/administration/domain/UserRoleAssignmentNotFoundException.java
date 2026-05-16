package yowyob.comops.api.administration.domain;

import java.util.UUID;

public final class UserRoleAssignmentNotFoundException extends RuntimeException {
    public UserRoleAssignmentNotFoundException(UUID assignmentId) {
        super("User role assignment " + assignmentId + " was not found.");
    }
}
