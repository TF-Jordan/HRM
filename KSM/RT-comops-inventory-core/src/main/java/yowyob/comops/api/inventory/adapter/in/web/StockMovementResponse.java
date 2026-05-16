package yowyob.comops.api.inventory.adapter.in.web;

import yowyob.comops.api.inventory.domain.model.StockMovement;
import java.math.BigDecimal;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        UUID productId,
        UUID thirdPartyId,
        String referenceNumber,
        String sourceDocumentType,
        String sourceDocumentNumber,
        String movementType,
        BigDecimal quantity,
        String status) {

    public static StockMovementResponse from(StockMovement stockMovement) {
        return new StockMovementResponse(stockMovement.id(), stockMovement.tenantId(), stockMovement.organizationId(),
                stockMovement.agencyId(), stockMovement.productId(), stockMovement.thirdPartyId(),
                stockMovement.referenceNumber(), stockMovement.sourceDocumentType(),
                stockMovement.sourceDocumentNumber(), stockMovement.movementType(), stockMovement.quantity(),
                stockMovement.status());
    }
}
