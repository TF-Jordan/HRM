package yowyob.comops.api.billing.application.service;

import java.util.UUID;
import yowyob.comops.api.kernel.domain.model.TenantContext;

public record BillingRequestContext(
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        UUID userId,
        UUID actorId) {

    public static BillingRequestContext from(TenantContext context) {
        return new BillingRequestContext(
                context.tenantId(),
                context.organizationId(),
                context.agencyId(),
                context.userId(),
                context.actorId());
    }

    public UUID requireOrganizationId() {
        if (organizationId == null) {
            throw new IllegalArgumentException("organization id is required");
        }
        return organizationId;
    }
}
