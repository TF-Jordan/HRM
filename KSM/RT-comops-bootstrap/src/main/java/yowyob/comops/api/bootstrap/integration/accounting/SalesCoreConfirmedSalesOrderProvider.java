package yowyob.comops.api.bootstrap.integration.accounting;

import yowyob.comops.api.accounting.application.port.out.ConfirmedSalesOrderLineSnapshot;
import yowyob.comops.api.accounting.application.port.out.ConfirmedSalesOrderProvider;
import yowyob.comops.api.accounting.application.port.out.ConfirmedSalesOrderSnapshot;
import yowyob.comops.api.sales.application.port.in.GetSalesOrderUseCase;
import yowyob.comops.api.sales.domain.model.SalesOrder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Component
public class SalesCoreConfirmedSalesOrderProvider implements ConfirmedSalesOrderProvider {

    private final GetSalesOrderUseCase getSalesOrderUseCase;

    public SalesCoreConfirmedSalesOrderProvider(GetSalesOrderUseCase getSalesOrderUseCase) {
        this.getSalesOrderUseCase = getSalesOrderUseCase;
    }

    @Override
    public Mono<ConfirmedSalesOrderSnapshot> getConfirmedOrder(UUID tenantId, UUID orderId) {
        return getSalesOrderUseCase.getById(orderId)
                .filter(order -> order.tenantId().equals(tenantId))
                .filter(order -> "CONFIRMED".equals(order.status()))
                .map(this::toSnapshot);
    }

    private ConfirmedSalesOrderSnapshot toSnapshot(SalesOrder order) {
        return new ConfirmedSalesOrderSnapshot(order.id(), order.tenantId(), order.organizationId(),
                order.customerThirdPartyId(), order.currency(), order.lines().stream()
                        .map(line -> new ConfirmedSalesOrderLineSnapshot(line.productId(), line.quantity(),
                                line.unitPrice()))
                        .toList());
    }
}
