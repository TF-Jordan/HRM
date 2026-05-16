package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.InvoiceSettlement;
import reactor.core.publisher.Mono;

public interface RegisterInvoiceSettlementUseCase {

    Mono<InvoiceSettlement> registerSettlement(RegisterInvoiceSettlementCommand command);
}
