package yowyob.comops.api.treasury.application.port.out;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface PostedInvoiceSettlementSourceProvider {

    Mono<PostedInvoiceSettlementSource> getPostedInvoice(UUID invoiceId);
}
