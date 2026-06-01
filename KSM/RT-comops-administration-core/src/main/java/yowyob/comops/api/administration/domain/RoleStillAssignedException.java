package yowyob.comops.api.administration.domain;

import java.util.UUID;

public final class RoleStillAssignedException extends RuntimeException {
    public RoleStillAssignedException(UUID roleId) {
        super("Role " + roleId + " still has active assignments.");
    }
}
