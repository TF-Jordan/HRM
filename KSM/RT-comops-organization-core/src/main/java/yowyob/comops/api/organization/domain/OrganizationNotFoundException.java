package yowyob.comops.api.organization.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public class OrganizationNotFoundException extends DomainException {

    public OrganizationNotFoundException(UUID organizationId) {
        super("Organization " + organizationId + " was not found.");
    }
}
