package yowyob.comops.api.sales.application.port.in;

import yowyob.comops.api.sales.domain.model.SalesOrder;
import reactor.core.publisher.Mono;

public interface CreateSalesOrderUseCase {

    Mono<SalesOrder> createOrder(CreateSalesOrderCommand command);
}
