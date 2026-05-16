package yowyob.comops.api.tp.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class ProspectConversionNotAllowedException extends DomainException {
    public ProspectConversionNotAllowedException(UUID thirdPartyId) {
        super("Third-party is not an active prospect and cannot be converted: " + thirdPartyId);
    }
}
