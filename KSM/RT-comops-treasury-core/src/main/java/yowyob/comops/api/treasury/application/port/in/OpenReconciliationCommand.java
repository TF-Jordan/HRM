package yowyob.comops.api.treasury.application.port.in;

import java.util.UUID;

public record OpenReconciliationCommand(UUID tenantId, UUID organizationId, UUID bankAccountId, UUID statementId,
        String referenceNumber) {
}
