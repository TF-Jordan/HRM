package yowyob.comops.api.sales.application.port.out;

import yowyob.comops.api.sales.domain.model.SalesOrder;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SalesOrderRepository {

    Mono<Boolean> existsByOrderNumber(UUID tenantId, UUID organizationId, String orderNumber);

    Mono<SalesOrder> findById(UUID orderId);

    Flux<SalesOrder> findByOrganizationId(UUID tenantId, UUID organizationId);

    Mono<Boolean> existsByOrderNumberExcludingId(UUID tenantId, UUID organizationId, String orderNumber, UUID orderId);

    Mono<SalesOrder> save(SalesOrder salesOrder);

    Mono<Void> deleteById(UUID orderId);
}
