package yowyob.comops.api.sales.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

public record CreateSalesOrderRequest(
        java.util.UUID organizationId,
        java.util.UUID agencyId,
        java.util.UUID customerThirdPartyId,
        java.util.UUID productId,
        String orderNumber,
        @DecimalMin(value = "0.01") BigDecimal quantity,
        @DecimalMin(value = "0.01") BigDecimal unitPrice,
        @NotBlank String currency,
        List<CreateSalesOrderLineRequest> lines) {
}
