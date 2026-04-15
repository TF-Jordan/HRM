package yowyob.comops.api.bootstrap.integration.sales;

import yowyob.comops.api.inventory.application.port.in.DispatchSalesOrderCommand;
import yowyob.comops.api.inventory.application.port.in.DispatchSalesOrderLineCommand;
import yowyob.comops.api.inventory.application.port.in.DispatchSalesOrderUseCase;
import yowyob.comops.api.inventory.domain.InsufficientStockException;
import yowyob.comops.api.sales.application.port.out.SalesOrderStockDispatchGateway;
import yowyob.comops.api.sales.domain.InsufficientStockForSalesOrderException;
import yowyob.comops.api.sales.domain.model.SalesOrder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class InventorySalesOrderStockDispatchGateway implements SalesOrderStockDispatchGateway {

    private final DispatchSalesOrderUseCase dispatchSalesOrderUseCase;

    public InventorySalesOrderStockDispatchGateway(DispatchSalesOrderUseCase dispatchSalesOrderUseCase) {
        this.dispatchSalesOrderUseCase = dispatchSalesOrderUseCase;
    }

    @Override
    public Mono<Void> dispatchForConfirmedOrder(SalesOrder salesOrder) {
        return dispatchSalesOrderUseCase.dispatch(new DispatchSalesOrderCommand(
                        salesOrder.tenantId(),
                        salesOrder.organizationId(),
                        salesOrder.agencyId(),
                        salesOrder.customerThirdPartyId(),
                        salesOrder.id(),
                        salesOrder.orderNumber(),
                        salesOrder.lines().stream()
                                .map(line -> new DispatchSalesOrderLineCommand(line.productId(), line.quantity()))
                                .toList()))
                .onErrorMap(InsufficientStockException.class, exception -> new InsufficientStockForSalesOrderException(
                        salesOrder.id(),
                        exception.productId(),
                        exception.requiredQuantity(),
                        exception.availableQuantity()));
    }
}
