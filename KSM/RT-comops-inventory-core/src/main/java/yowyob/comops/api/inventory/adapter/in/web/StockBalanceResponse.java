package yowyob.comops.api.inventory.adapter.in.web;

import yowyob.comops.api.inventory.domain.model.StockBalance;
import java.math.BigDecimal;
import java.util.UUID;

public record StockBalanceResponse(UUID organizationId, UUID agencyId, UUID productId, BigDecimal onHandQuantity) {

    public static StockBalanceResponse from(StockBalance stockBalance) {
        return new StockBalanceResponse(stockBalance.organizationId(), stockBalance.agencyId(), stockBalance.productId(),
                stockBalance.onHandQuantity());
    }
}
