package yowyob.comops.api.accounting.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InvoiceLineSpringDataRepository extends ReactiveCrudRepository<InvoiceLineEntity, UUID> {

    Flux<InvoiceLineEntity> findAllByInvoiceIdOrderByCreatedAtAsc(UUID invoiceId);

    Mono<Void> deleteAllByInvoiceId(UUID invoiceId);
}
