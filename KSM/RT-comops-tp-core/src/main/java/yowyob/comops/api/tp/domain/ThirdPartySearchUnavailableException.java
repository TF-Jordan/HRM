package yowyob.comops.api.tp.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class ThirdPartySearchUnavailableException extends DomainException {

    public ThirdPartySearchUnavailableException() {
        super("Third party search is unavailable.");
    }
}
