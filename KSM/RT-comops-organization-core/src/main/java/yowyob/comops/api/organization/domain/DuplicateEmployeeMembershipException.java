package yowyob.comops.api.organization.domain;

import java.util.UUID;

public class DuplicateEmployeeMembershipException extends RuntimeException {

    public DuplicateEmployeeMembershipException(UUID userId, UUID organizationId) {
        super("User " + userId + " is already an employee of organization " + organizationId + ".");
    }
}
