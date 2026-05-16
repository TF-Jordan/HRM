package yowyob.comops.api.accounting.application.port.out;

import yowyob.comops.api.accounting.domain.model.Invoice;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InvoiceRepository {

    Mono<Boolean> existsByInvoiceNumber(UUID tenantId, UUID organizationId, String invoiceNumber);

    Mono<Boolean> existsByOrderId(UUID orderId);

    Mono<Invoice> findById(UUID invoiceId);

    Flux<Invoice> findByOrganizationId(UUID tenantId, UUID organizationId);

    Mono<Boolean> existsByInvoiceNumberExcludingId(UUID tenantId, UUID organizationId, String invoiceNumber,
            UUID invoiceId);

    Mono<Invoice> save(Invoice invoice);

    Mono<Void> deleteById(UUID invoiceId);
}
