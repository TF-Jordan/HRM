package yowyob.comops.api.accounting.application.port.in;

import yowyob.comops.api.accounting.domain.model.Invoice;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListInvoicesUseCase {
    Flux<Invoice> listInvoices(UUID tenantId, UUID organizationId);
}
