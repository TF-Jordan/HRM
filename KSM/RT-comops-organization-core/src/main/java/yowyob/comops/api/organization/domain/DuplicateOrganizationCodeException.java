package yowyob.comops.api.organization.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class DuplicateOrganizationCodeException extends DomainException {

    public DuplicateOrganizationCodeException(String code) {
        super("An organization already exists with code: " + code);
    }
}
