package yowyob.comops.api.resource.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class DuplicateResourceCodeException extends DomainException {

    public DuplicateResourceCodeException(String resourceCode) {
        super("A resource already exists with code: " + resourceCode);
    }
}
