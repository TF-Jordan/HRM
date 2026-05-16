package yowyob.comops.api.accounting.application.port.in;

import yowyob.comops.api.accounting.domain.model.Invoice;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface CreateInvoiceFromSalesOrderUseCase {
    Mono<Invoice> createFromSalesOrder(UUID tenantId, UUID orderId);
}
