package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.InvoiceSettlement;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface GetInvoiceSettlementUseCase {

    Mono<InvoiceSettlement> getSettlement(UUID settlementId);
}
