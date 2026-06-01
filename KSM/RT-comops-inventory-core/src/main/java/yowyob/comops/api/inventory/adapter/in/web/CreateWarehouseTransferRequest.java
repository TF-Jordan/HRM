package yowyob.comops.api.inventory.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateWarehouseTransferRequest(@NotNull UUID organizationId, @NotNull UUID sourceAgencyId,
        @NotNull UUID targetAgencyId, @NotNull UUID productId, String referenceNumber,
        @NotNull @DecimalMin("0.01") BigDecimal quantity) {
}
