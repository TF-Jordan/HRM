package yowyob.comops.api.inventory.application.port.in;

import reactor.core.publisher.Mono;

public interface DispatchSalesOrderUseCase {

    Mono<Void> dispatch(DispatchSalesOrderCommand command);
}
