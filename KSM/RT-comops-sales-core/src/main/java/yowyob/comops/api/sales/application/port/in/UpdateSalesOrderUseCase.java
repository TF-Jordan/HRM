package yowyob.comops.api.sales.application.port.in;

import yowyob.comops.api.sales.domain.model.SalesOrder;
import reactor.core.publisher.Mono;

public interface UpdateSalesOrderUseCase {

    Mono<SalesOrder> updateOrder(UpdateSalesOrderCommand command);
}
