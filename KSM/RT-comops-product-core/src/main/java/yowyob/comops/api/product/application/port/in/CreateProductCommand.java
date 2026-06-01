package yowyob.comops.api.product.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductCommand(
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
}
