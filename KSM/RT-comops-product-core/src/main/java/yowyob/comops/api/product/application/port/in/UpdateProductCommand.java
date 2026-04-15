package yowyob.comops.api.product.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductCommand(
        UUID tenantId,
        UUID productId,
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
