package yowyob.comops.api.accounting.adapter.out.persistence;

import yowyob.comops.api.accounting.application.port.out.InvoiceRepository;
import yowyob.comops.api.accounting.domain.model.Invoice;
import yowyob.comops.api.accounting.domain.model.InvoiceLine;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class InvoiceR2dbcRepositoryAdapter implements InvoiceRepository {

    private final InvoiceSpringDataRepository repository;
    private final InvoiceLineSpringDataRepository lineRepository;

    public InvoiceR2dbcRepositoryAdapter(InvoiceSpringDataRepository repository,
            InvoiceLineSpringDataRepository lineRepository) {
        this.repository = repository;
        this.lineRepository = lineRepository;
    }

    @Override
    public Mono<Boolean> existsByInvoiceNumber(UUID tenantId, UUID organizationId, String invoiceNumber) {
        return repository.existsByTenantIdAndOrganizationIdAndInvoiceNumberIgnoreCase(tenantId, organizationId,
                invoiceNumber);
    }

    @Override
    public Mono<Boolean> existsByOrderId(UUID orderId) {
        if (orderId == null) {
            return Mono.just(false);
        }
        return repository.existsByOrderId(orderId);
    }

    @Override
    public Mono<Invoice> findById(UUID invoiceId) {
        return repository.findById(invoiceId)
                .flatMap(this::toDomain);
    }

    @Override
    public Flux<Invoice> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .flatMap(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByInvoiceNumberExcludingId(UUID tenantId, UUID organizationId, String invoiceNumber,
            UUID invoiceId) {
        return repository.existsByTenantIdAndOrganizationIdAndInvoiceNumberIgnoreCaseAndIdNot(tenantId, organizationId,
                invoiceNumber, invoiceId);
    }

    @Override
    public Mono<Invoice> save(Invoice invoice) {
        InvoiceEntity entity = new InvoiceEntity(invoice.id(), invoice.tenantId(), invoice.createdAt(),
                invoice.updatedAt(), invoice.organizationId(), invoice.customerThirdPartyId(), invoice.orderId(),
                invoice.productId(), invoice.invoiceNumber(), invoice.quantity(), invoice.unitPrice(),
                invoice.totalQuantity(), invoice.subtotalAmount(), invoice.totalAmount(), invoice.currency(),
                invoice.status(), invoice.paymentStatus(), invoice.settledAmount(), invoice.outstandingAmount(),
                invoice.settledAt());
        java.time.Instant lineTimestamp = invoice.updatedAt();
        List<InvoiceLineEntity> lineEntities = invoice.lines().stream()
                .map(line -> new InvoiceLineEntity(UUID.randomUUID(), invoice.id(), invoice.tenantId(),
                        lineTimestamp, lineTimestamp, line.productId(), line.quantity(),
                        line.unitPrice(), line.lineAmount()))
                .toList();
        return repository.save(entity)
                .flatMap(saved -> lineRepository.deleteAllByInvoiceId(saved.id())
                        .thenMany(Flux.fromIterable(lineEntities))
                        .flatMap(lineRepository::save)
                        .then(Mono.just(saved)))
                .flatMap(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID invoiceId) {
        return lineRepository.deleteAllByInvoiceId(invoiceId)
                .then(repository.deleteById(invoiceId));
    }

    private Mono<Invoice> toDomain(InvoiceEntity entity) {
        return lineRepository.findAllByInvoiceIdOrderByCreatedAtAsc(entity.id())
                .map(this::toDomainLine)
                .collectList()
                .map(lines -> Invoice.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(),
                        entity.updatedAt(), entity.organizationId(), entity.customerThirdPartyId(), entity.orderId(),
                        entity.invoiceNumber(), entity.currency(), entity.status(), entity.paymentStatus(), lines,
                        entity.totalQuantity(), entity.subtotalAmount(), entity.totalAmount(), entity.settledAmount(),
                        entity.outstandingAmount(), entity.settledAt()));
    }

    private InvoiceLine toDomainLine(InvoiceLineEntity entity) {
        return new InvoiceLine(entity.productId(), entity.quantity(), entity.unitPrice(), entity.lineAmount());
    }
}
