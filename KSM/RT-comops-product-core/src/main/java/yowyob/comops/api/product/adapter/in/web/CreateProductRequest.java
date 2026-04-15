package yowyob.comops.api.product.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(
        @NotNull UUID organizationId,
        @NotBlank String sku,
        @NotBlank String name,
        @NotBlank String familyCode,
        String categoryCode,
        @NotBlank String variantLabel,
        String barcode,
        String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal unitPrice,
        @NotBlank String currency,
        String status) {
}
