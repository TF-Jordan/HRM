package yowyob.comops.api.organization.domain;

import yowyob.comops.api.common.domain.DomainException;

public class DuplicateAgencyCodeException extends DomainException {

    public DuplicateAgencyCodeException(String code) {
        super("An agency with code " + code + " already exists in this organization.");
    }
}
