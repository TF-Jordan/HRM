package yowyob.comops.api.inventory.application.port.in;

import yowyob.comops.api.inventory.domain.model.StockBalance;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface GetStockBalanceUseCase {

    Mono<StockBalance> getBalance(UUID organizationId, UUID agencyId, UUID productId);
}
