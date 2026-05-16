package yowyob.comops.api.inventory.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record RecordStockMovementRequest(
        @NotNull UUID organizationId,
        @NotNull UUID agencyId,
        @NotNull UUID productId,
        UUID thirdPartyId,
        String referenceNumber,
        String sourceDocumentType,
        String sourceDocumentNumber,
        @NotBlank String movementType,
        @NotNull @DecimalMin(value = "0.01") BigDecimal quantity) {
}
