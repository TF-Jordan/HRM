package yowyob.comops.api.organization.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class DuplicatePhysicalSpaceCodeException extends DomainException {

    public DuplicatePhysicalSpaceCodeException(String code) {
        super("Physical space code already exists: " + code);
    }
}
