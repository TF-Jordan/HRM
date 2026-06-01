package yowyob.comops.api.inventory.adapter.in.web;

import yowyob.comops.api.inventory.domain.model.ProductTransformation;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductTransformationResponse(UUID id, UUID tenantId, UUID organizationId, UUID agencyId, UUID sourceProductId,
        UUID targetProductId, String referenceNumber, BigDecimal sourceQuantity, BigDecimal targetQuantity, String status) {
    public static ProductTransformationResponse from(ProductTransformation transformation) {
        return new ProductTransformationResponse(transformation.id(), transformation.tenantId(), transformation.organizationId(),
                transformation.agencyId(), transformation.sourceProductId(), transformation.targetProductId(),
                transformation.referenceNumber(), transformation.sourceQuantity(), transformation.targetQuantity(),
                transformation.status());
    }
}
