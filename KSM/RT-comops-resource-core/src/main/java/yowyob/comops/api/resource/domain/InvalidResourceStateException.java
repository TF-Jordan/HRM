package yowyob.comops.api.resource.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class InvalidResourceStateException extends DomainException {
    public InvalidResourceStateException(UUID resourceId, String currentStatus, String action) {
        super("Resource " + resourceId + " in status " + currentStatus + " cannot perform action " + action + '.');
    }
}
