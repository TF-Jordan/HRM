package yowyob.comops.api.organization.domain;

import yowyob.comops.api.common.domain.DomainException;

public class DuplicatePointOfInterestException extends DomainException {

    public DuplicatePointOfInterestException(String name) {
        super("A point of interest named " + name + " already exists for this agency.");
    }
}
