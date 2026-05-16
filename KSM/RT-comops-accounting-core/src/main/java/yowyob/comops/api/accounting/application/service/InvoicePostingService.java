package yowyob.comops.api.accounting.application.service;

import yowyob.comops.api.accounting.application.port.in.PostInvoiceUseCase;
import yowyob.comops.api.accounting.application.port.out.InvoiceRepository;
import yowyob.comops.api.accounting.domain.InvoiceNotFoundException;
import yowyob.comops.api.accounting.domain.model.Invoice;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class InvoicePostingService implements PostInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public InvoicePostingService(InvoiceRepository invoiceRepository,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.invoiceRepository = invoiceRepository;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<Invoice> post(UUID invoiceId) {
        return transactionalExecutor.transactional(invoiceRepository.findById(invoiceId)
                .switchIfEmpty(Mono.error(new InvoiceNotFoundException(invoiceId)))
                .map(Invoice::post)
                .flatMap(invoiceRepository::save)
                .flatMap(saved -> businessEventPublisher.publish(invoicePostedEvent(saved)).thenReturn(saved)));
    }

    private BusinessEvent invoicePostedEvent(Invoice invoice) {
        return BusinessEvent.now(invoice.tenantId(), invoice.organizationId(), "INVOICE_POSTED", "INVOICE",
                invoice.id(), payload(
                        "invoiceNumber", invoice.invoiceNumber(),
                        "orderId", invoice.orderId(),
                        "status", invoice.status(),
                        "paymentStatus", invoice.paymentStatus(),
                        "outstandingAmount", invoice.outstandingAmount(),
                        "totalAmount", invoice.totalAmount()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
