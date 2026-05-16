package yowyob.comops.api.product.adapter.in.web;

import yowyob.comops.api.product.domain.model.Product;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        String sku,
        String name,
        String familyCode,
        String categoryCode,
        String variantLabel,
        String barcode,
        String description,
        BigDecimal unitPrice,
        String currency,
        String status) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.id(), product.tenantId(), product.organizationId(), product.sku(),
                product.name(), product.familyCode(), product.categoryCode(), product.variantLabel(), product.barcode(),
                product.description(), product.unitPrice(), product.currency(), product.status());
    }
}
