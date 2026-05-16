package yowyob.comops.api.hrm.domain;

import yowyob.comops.api.common.domain.DomainException;

import java.util.UUID;

public final class ActorNotFoundException extends DomainException {
    public ActorNotFoundException(UUID actorId) {
        super("Actor not found: " + actorId);
    }
}
