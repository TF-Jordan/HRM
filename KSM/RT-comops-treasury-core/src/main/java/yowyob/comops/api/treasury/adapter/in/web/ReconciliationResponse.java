package yowyob.comops.api.treasury.adapter.in.web;

import yowyob.comops.api.treasury.domain.model.Reconciliation;
import java.time.Instant;
import java.util.UUID;

public record ReconciliationResponse(UUID id, UUID tenantId, UUID organizationId, UUID bankAccountId, UUID statementId,
        String referenceNumber, String status, Instant closedAt) {
    public static ReconciliationResponse from(Reconciliation reconciliation) {
        return new ReconciliationResponse(reconciliation.id(), reconciliation.tenantId(), reconciliation.organizationId(),
                reconciliation.bankAccountId(), reconciliation.statementId(), reconciliation.referenceNumber(),
                reconciliation.status(), reconciliation.closedAt());
    }
}
