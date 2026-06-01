package yowyob.comops.api.resource.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class ResourceSearchUnavailableException extends DomainException {

    public ResourceSearchUnavailableException() {
        super("Resource search is unavailable.");
    }
}
