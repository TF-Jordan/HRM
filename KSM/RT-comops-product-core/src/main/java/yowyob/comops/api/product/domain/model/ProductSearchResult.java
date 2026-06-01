package yowyob.comops.api.product.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSearchResult(
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
}
