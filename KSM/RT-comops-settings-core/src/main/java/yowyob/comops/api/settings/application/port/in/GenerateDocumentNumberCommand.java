package yowyob.comops.api.settings.application.port.in;

import java.util.UUID;

public record GenerateDocumentNumberCommand(
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        String documentType) {
}
