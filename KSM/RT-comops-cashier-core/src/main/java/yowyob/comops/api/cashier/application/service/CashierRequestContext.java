package yowyob.comops.api.cashier.application.service;

import java.util.UUID;
import yowyob.comops.api.kernel.domain.model.TenantContext;

public record CashierRequestContext(UUID tenantId, UUID organizationId, UUID agencyId, UUID userId, UUID actorId) {

    public static CashierRequestContext from(TenantContext context) {
        return new CashierRequestContext(context.tenantId(), context.organizationId(), context.agencyId(),
                context.userId(), context.actorId());
    }

    public UUID requireOrganizationId() {
        if (organizationId == null) {
            throw new IllegalStateException("organizationId is required");
        }
        return organizationId;
    }
}
