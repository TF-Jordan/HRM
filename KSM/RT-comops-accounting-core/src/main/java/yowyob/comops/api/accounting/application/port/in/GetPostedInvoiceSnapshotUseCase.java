package yowyob.comops.api.accounting.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface GetPostedInvoiceSnapshotUseCase {

    Mono<PostedInvoiceSnapshot> getPostedInvoice(UUID invoiceId);
}
