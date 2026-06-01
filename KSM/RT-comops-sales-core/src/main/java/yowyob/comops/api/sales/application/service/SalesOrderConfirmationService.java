package yowyob.comops.api.sales.application.service;

import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.sales.application.port.in.ConfirmSalesOrderUseCase;
import yowyob.comops.api.sales.application.port.in.CancelSalesOrderUseCase;
import yowyob.comops.api.sales.application.port.in.DeleteSalesOrderUseCase;
import yowyob.comops.api.sales.application.port.in.GetSalesOrderUseCase;
import yowyob.comops.api.sales.application.port.in.ListSalesOrdersUseCase;
import yowyob.comops.api.sales.application.port.out.SalesOrderRepository;
import yowyob.comops.api.sales.application.port.out.SalesOrderStockDispatchGateway;
import yowyob.comops.api.sales.domain.SalesOrderNotFoundException;
import yowyob.comops.api.sales.domain.model.SalesOrder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@Service
public class SalesOrderConfirmationService implements ConfirmSalesOrderUseCase, GetSalesOrderUseCase, ListSalesOrdersUseCase,
        CancelSalesOrderUseCase, DeleteSalesOrderUseCase {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderStockDispatchGateway salesOrderStockDispatchGateway;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public SalesOrderConfirmationService(SalesOrderRepository salesOrderRepository,
            SalesOrderStockDispatchGateway salesOrderStockDispatchGateway,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderStockDispatchGateway = salesOrderStockDispatchGateway;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<SalesOrder> confirm(UUID orderId) {
        return transactionalExecutor.transactional(salesOrderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new SalesOrderNotFoundException(orderId)))
                .flatMap(order -> salesOrderStockDispatchGateway.dispatchForConfirmedOrder(order).thenReturn(order))
                .map(SalesOrder::confirm)
                .flatMap(salesOrderRepository::save)
                .flatMap(saved -> businessEventPublisher.publish(orderConfirmedEvent(saved)).thenReturn(saved)));
    }

    @Override
    public Mono<SalesOrder> getById(UUID orderId) {
        return salesOrderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new SalesOrderNotFoundException(orderId)));
    }

    @Override
    public Flux<SalesOrder> listByOrganization(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> salesOrderRepository.findByOrganizationId(context.tenantId(), organizationId));
    }

    @Override
    public Mono<SalesOrder> cancel(UUID orderId) {
        return transactionalExecutor.transactional(salesOrderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new SalesOrderNotFoundException(orderId)))
                .map(SalesOrder::cancel)
                .flatMap(salesOrderRepository::save));
    }

    @Override
    public Mono<Void> delete(UUID orderId) {
        return transactionalExecutor.transactional(salesOrderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new SalesOrderNotFoundException(orderId)))
                .flatMap(existing -> salesOrderRepository.deleteById(orderId)));
    }

    private BusinessEvent orderConfirmedEvent(SalesOrder order) {
        return BusinessEvent.now(order.tenantId(), order.organizationId(), "SALES_ORDER_CONFIRMED", "SALES_ORDER",
                order.id(), payload(
                        "orderNumber", order.orderNumber(),
                        "agencyId", order.agencyId(),
                        "customerThirdPartyId", order.customerThirdPartyId(),
                        "status", order.status(),
                        "totalAmount", order.totalAmount()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
