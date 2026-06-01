package yowyob.comops.api.tp.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class DuplicateThirdPartyReferenceException extends DomainException {

    public DuplicateThirdPartyReferenceException(String referenceCode) {
        super("A third party already exists with reference: " + referenceCode);
    }
}
