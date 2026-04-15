package yowyob.comops.api.organization.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class PhysicalSpaceNotFoundException extends DomainException {

    public PhysicalSpaceNotFoundException(UUID spaceId) {
        super("Physical space not found: " + spaceId);
    }
}
