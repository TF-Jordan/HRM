package yowyob.comops.api.settings.application.port.in;

import java.util.UUID;

public record UpsertDocumentSequenceCommand(
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        String documentType,
        String prefix,
        String suffix,
        int paddingWidth,
        long nextNumber) {
}
