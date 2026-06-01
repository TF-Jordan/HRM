package yowyob.comops.api.accounting.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface DeleteInvoiceUseCase {

    Mono<Void> deleteInvoice(UUID invoiceId);
}
