package yowyob.comops.api.organization.domain;

import java.util.UUID;

public class EmployeeMembershipNotFoundException extends RuntimeException {

    public EmployeeMembershipNotFoundException(UUID membershipId) {
        super("Employee membership " + membershipId + " was not found.");
    }
}
