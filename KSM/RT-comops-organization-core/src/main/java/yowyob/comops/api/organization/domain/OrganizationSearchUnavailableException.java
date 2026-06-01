package yowyob.comops.api.organization.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class OrganizationSearchUnavailableException extends DomainException {

    public OrganizationSearchUnavailableException() {
        super("Organization search is not available in the current runtime profile.");
    }
}
