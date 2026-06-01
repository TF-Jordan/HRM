package yowyob.comops.api.sales.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateSalesOrderLineRequest(
        @NotNull UUID productId,
        @NotNull @DecimalMin("0.01") BigDecimal quantity,
        @NotNull @DecimalMin("0.01") BigDecimal unitPrice) {
}
