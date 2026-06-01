package yowyob.comops.api.tp.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class ThirdPartyLookupNotFoundException extends DomainException {
    public ThirdPartyLookupNotFoundException(String criterion, String value) {
        super("Third-party not found by " + criterion + ": " + value);
    }
}
