package yowyob.comops.api.inventory.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record RecordTransformationRequest(@NotNull UUID organizationId, @NotNull UUID agencyId, @NotNull UUID sourceProductId,
        @NotNull UUID targetProductId, String referenceNumber, @NotNull @DecimalMin("0.01") BigDecimal sourceQuantity,
        @NotNull @DecimalMin("0.01") BigDecimal targetQuantity) {
}
