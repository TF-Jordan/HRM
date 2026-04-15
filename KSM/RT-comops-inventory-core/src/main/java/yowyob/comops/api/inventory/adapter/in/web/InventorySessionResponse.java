package yowyob.comops.api.inventory.adapter.in.web;

import yowyob.comops.api.inventory.domain.model.InventorySession;
import java.math.BigDecimal;
import java.util.UUID;

public record InventorySessionResponse(UUID id, UUID tenantId, UUID organizationId, UUID agencyId, UUID productId,
        String referenceNumber, BigDecimal countedQuantity, String status) {

    public static InventorySessionResponse from(InventorySession session) {
        return new InventorySessionResponse(session.id(), session.tenantId(), session.organizationId(),
                session.agencyId(), session.productId(), session.referenceNumber(), session.countedQuantity(),
                session.status());
    }
}
