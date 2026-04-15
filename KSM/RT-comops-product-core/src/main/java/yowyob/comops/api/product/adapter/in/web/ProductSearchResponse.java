package yowyob.comops.api.product.adapter.in.web;

import yowyob.comops.api.product.domain.model.ProductSearchResult;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductSearchResponse(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        String sku,
        String name,
        String familyCode,
        String variantLabel,
        String barcode,
        String description,
        BigDecimal unitPrice,
        String currency,
        String status) {

    public static ProductSearchResponse from(ProductSearchResult result) {
        return new ProductSearchResponse(result.id(), result.tenantId(), result.organizationId(), result.sku(),
                result.name(), result.familyCode(), result.variantLabel(), result.barcode(), result.description(),
                result.unitPrice(), result.currency(), result.status());
    }
}
