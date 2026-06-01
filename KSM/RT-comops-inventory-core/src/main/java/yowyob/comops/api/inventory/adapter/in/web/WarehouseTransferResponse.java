package yowyob.comops.api.inventory.adapter.in.web;

import yowyob.comops.api.inventory.domain.model.WarehouseTransfer;
import java.math.BigDecimal;
import java.util.UUID;

public record WarehouseTransferResponse(UUID id, UUID tenantId, UUID organizationId, UUID sourceAgencyId,
        UUID targetAgencyId, UUID productId, String referenceNumber, BigDecimal quantity, String status) {
    public static WarehouseTransferResponse from(WarehouseTransfer transfer) {
        return new WarehouseTransferResponse(transfer.id(), transfer.tenantId(), transfer.organizationId(), transfer.sourceAgencyId(),
                transfer.targetAgencyId(), transfer.productId(), transfer.referenceNumber(), transfer.quantity(), transfer.status());
    }
}
