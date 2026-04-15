package yowyob.comops.api.sales.application.port.in;

import yowyob.comops.api.sales.domain.model.SalesOrder;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ConfirmSalesOrderUseCase {

    Mono<SalesOrder> confirm(UUID orderId);
}
