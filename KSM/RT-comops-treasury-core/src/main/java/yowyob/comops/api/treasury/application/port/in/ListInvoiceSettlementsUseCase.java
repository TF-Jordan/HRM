package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.InvoiceSettlement;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListInvoiceSettlementsUseCase {

    Flux<InvoiceSettlement> listSettlements(UUID tenantId, UUID organizationId, UUID invoiceId);
}
