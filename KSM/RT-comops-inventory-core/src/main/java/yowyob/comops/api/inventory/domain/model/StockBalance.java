package yowyob.comops.api.inventory.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record StockBalance(UUID organizationId, UUID agencyId, UUID productId, BigDecimal onHandQuantity) {

    public StockBalance {
        Objects.requireNonNull(organizationId, "organizationId is required");
        Objects.requireNonNull(agencyId, "agencyId is required");
        Objects.requireNonNull(productId, "productId is required");
        Objects.requireNonNull(onHandQuantity, "onHandQuantity is required");
    }
}
