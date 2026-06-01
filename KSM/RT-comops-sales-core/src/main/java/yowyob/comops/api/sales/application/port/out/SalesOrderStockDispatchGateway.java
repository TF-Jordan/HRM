package yowyob.comops.api.sales.application.port.out;

import yowyob.comops.api.sales.domain.model.SalesOrder;
import reactor.core.publisher.Mono;

public interface SalesOrderStockDispatchGateway {

    Mono<Void> dispatchForConfirmedOrder(SalesOrder salesOrder);
}
