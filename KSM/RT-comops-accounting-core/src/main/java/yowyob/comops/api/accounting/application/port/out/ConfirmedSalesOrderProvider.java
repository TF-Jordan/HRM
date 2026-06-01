package yowyob.comops.api.accounting.application.port.out;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ConfirmedSalesOrderProvider {
    Mono<ConfirmedSalesOrderSnapshot> getConfirmedOrder(UUID tenantId, UUID orderId);
}
