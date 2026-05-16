package yowyob.comops.api.accounting.application.port.in;

import yowyob.comops.api.accounting.domain.model.Invoice;
import reactor.core.publisher.Mono;

public interface ApplyInvoiceSettlementUseCase {

    Mono<Invoice> apply(ApplyInvoiceSettlementCommand command);
}
