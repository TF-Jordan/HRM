package yowyob.comops.api.inventory.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateInventorySessionRequest(
        @NotNull UUID organizationId,
        @NotNull UUID agencyId,
        @NotNull UUID productId,
        String referenceNumber,
        @NotNull @DecimalMin(value = "0.01") BigDecimal countedQuantity) {
}
