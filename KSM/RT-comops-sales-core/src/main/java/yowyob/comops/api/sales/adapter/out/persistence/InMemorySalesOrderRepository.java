package yowyob.comops.api.sales.adapter.out.persistence;

import yowyob.comops.api.sales.application.port.out.SalesOrderRepository;
import yowyob.comops.api.sales.domain.model.SalesOrder;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemorySalesOrderRepository implements SalesOrderRepository {

    private final Map<UUID, SalesOrder> salesOrders = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsByOrderNumber(UUID tenantId, UUID organizationId, String orderNumber) {
        return Mono.fromSupplier(() -> salesOrders.values().stream()
                .filter(order -> order.tenantId().equals(tenantId))
                .filter(order -> order.organizationId().equals(organizationId))
                .anyMatch(order -> order.orderNumber().equalsIgnoreCase(orderNumber)));
    }

    @Override
    public Mono<SalesOrder> findById(UUID orderId) {
        return Mono.justOrEmpty(salesOrders.get(orderId));
    }

    @Override
    public Flux<SalesOrder> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(salesOrders.values().stream()
                .filter(order -> order.tenantId().equals(tenantId))
                .filter(order -> order.organizationId().equals(organizationId)));
    }

    @Override
    public Mono<Boolean> existsByOrderNumberExcludingId(UUID tenantId, UUID organizationId, String orderNumber, UUID orderId) {
        return Mono.fromSupplier(() -> salesOrders.values().stream()
                .filter(order -> order.tenantId().equals(tenantId))
                .filter(order -> order.organizationId().equals(organizationId))
                .filter(order -> !order.id().equals(orderId))
                .anyMatch(order -> order.orderNumber().equalsIgnoreCase(orderNumber)));
    }

    @Override
    public Mono<SalesOrder> save(SalesOrder salesOrder) {
        return Mono.fromSupplier(() -> {
            salesOrders.put(salesOrder.id(), salesOrder);
            return salesOrder;
        });
    }

    @Override
    public Mono<Void> deleteById(UUID orderId) {
        return Mono.fromRunnable(() -> salesOrders.remove(orderId));
    }
}
