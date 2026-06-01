package yowyob.comops.api.sales.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface DeleteSalesOrderUseCase {

    Mono<Void> delete(UUID orderId);
}
