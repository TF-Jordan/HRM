package yowyob.comops.api.hrm.domain;

import yowyob.comops.api.common.domain.DomainException;

import java.util.UUID;

public final class DuplicateEmployeeException extends DomainException {
    public DuplicateEmployeeException(UUID actorId) {
        super("Employee already exists for actor: " + actorId);
    }
}
