package yowyob.comops.api.accounting.adapter.out.persistence;

import yowyob.comops.api.accounting.application.port.out.InvoiceRepository;
import yowyob.comops.api.accounting.domain.model.Invoice;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryInvoiceRepository implements InvoiceRepository {

    private final Map<UUID, Invoice> invoices = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsByInvoiceNumber(UUID tenantId, UUID organizationId, String invoiceNumber) {
        return Mono.fromSupplier(() -> invoices.values().stream()
                .filter(invoice -> invoice.tenantId().equals(tenantId))
                .filter(invoice -> invoice.organizationId().equals(organizationId))
                .anyMatch(invoice -> invoice.invoiceNumber().equalsIgnoreCase(invoiceNumber)));
    }

    @Override
    public Mono<Boolean> existsByOrderId(UUID orderId) {
        if (orderId == null) {
            return Mono.just(false);
        }
        return Mono.fromSupplier(() -> invoices.values().stream()
                .anyMatch(invoice -> orderId.equals(invoice.orderId())));
    }

    @Override
    public Mono<Invoice> findById(UUID invoiceId) {
        return Mono.justOrEmpty(invoices.get(invoiceId));
    }

    @Override
    public Flux<Invoice> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(invoices.values().stream()
                .filter(invoice -> invoice.tenantId().equals(tenantId))
                .filter(invoice -> invoice.organizationId().equals(organizationId))
                .sorted((left, right) -> left.createdAt().compareTo(right.createdAt())));
    }

    @Override
    public Mono<Boolean> existsByInvoiceNumberExcludingId(UUID tenantId, UUID organizationId, String invoiceNumber,
            UUID invoiceId) {
        return Mono.fromSupplier(() -> invoices.values().stream()
                .filter(invoice -> invoice.tenantId().equals(tenantId))
                .filter(invoice -> invoice.organizationId().equals(organizationId))
                .filter(invoice -> !invoice.id().equals(invoiceId))
                .anyMatch(invoice -> invoice.invoiceNumber().equalsIgnoreCase(invoiceNumber)));
    }

    @Override
    public Mono<Invoice> save(Invoice invoice) {
        return Mono.fromSupplier(() -> {
            invoices.put(invoice.id(), invoice);
            return invoice;
        });
    }

    @Override
    public Mono<Void> deleteById(UUID invoiceId) {
        return Mono.fromRunnable(() -> invoices.remove(invoiceId));
    }
}
