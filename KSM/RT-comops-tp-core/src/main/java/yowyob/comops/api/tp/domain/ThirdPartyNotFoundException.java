package yowyob.comops.api.tp.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public class ThirdPartyNotFoundException extends DomainException {

    public ThirdPartyNotFoundException(UUID thirdPartyId) {
        super("Third party " + thirdPartyId + " was not found.");
    }
}
