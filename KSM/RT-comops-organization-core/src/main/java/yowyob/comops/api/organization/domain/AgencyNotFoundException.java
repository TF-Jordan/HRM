package yowyob.comops.api.organization.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public class AgencyNotFoundException extends DomainException {

    public AgencyNotFoundException(UUID agencyId) {
        super("Agency " + agencyId + " was not found.");
    }
}
