package yowyob.comops.api.settings.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class DocumentSequenceNotFoundException extends DomainException {

    public DocumentSequenceNotFoundException(String documentType) {
        super("No document sequence configured for type: " + documentType);
    }
}
