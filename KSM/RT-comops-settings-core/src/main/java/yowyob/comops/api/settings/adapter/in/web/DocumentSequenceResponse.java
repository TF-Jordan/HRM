package yowyob.comops.api.settings.adapter.in.web;

import yowyob.comops.api.settings.domain.model.DocumentSequence;
import java.util.UUID;

public record DocumentSequenceResponse(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        String documentType,
        String prefix,
        String suffix,
        int paddingWidth,
        long nextNumber) {

    public static DocumentSequenceResponse from(DocumentSequence documentSequence) {
        return new DocumentSequenceResponse(documentSequence.id(), documentSequence.tenantId(),
                documentSequence.organizationId(), documentSequence.agencyId(), documentSequence.documentType(),
                documentSequence.prefix(), documentSequence.suffix(), documentSequence.paddingWidth(),
                documentSequence.nextNumber());
    }
}
