package yowyob.comops.api.accounting.extension.service;

import yowyob.comops.api.kernel.domain.model.TenantContext;
import java.util.UUID;

public record AccountingExtensionRequestContext(UUID tenantId, UUID organizationId, UUID agencyId, UUID userId, UUID actorId) {

    public static AccountingExtensionRequestContext from(TenantContext context) {
        return new AccountingExtensionRequestContext(context.tenantId(), context.organizationId(), context.agencyId(), context.userId(), context.actorId());
    }

    public UUID requireOrganizationId() {
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId is required");
        }
        return organizationId;
    }

    public UUID requireTenantId() {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        return tenantId;
    }
}
