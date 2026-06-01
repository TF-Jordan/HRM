package yowyob.comops.api.accounting.application.port.in;

import yowyob.comops.api.accounting.domain.model.Invoice;
import reactor.core.publisher.Mono;

public interface CreateInvoiceUseCase {

    Mono<Invoice> createInvoice(CreateInvoiceCommand command);
}
