package yowyob.comops.api.accounting.application.service;

import yowyob.comops.api.accounting.application.port.in.ApplyInvoiceSettlementCommand;
import yowyob.comops.api.accounting.application.port.in.ApplyInvoiceSettlementUseCase;
import yowyob.comops.api.accounting.application.port.in.GetPostedInvoiceSnapshotUseCase;
import yowyob.comops.api.accounting.application.port.in.PostedInvoiceSnapshot;
import yowyob.comops.api.accounting.application.port.out.InvoiceRepository;
import yowyob.comops.api.accounting.domain.InvoiceNotFoundException;
import yowyob.comops.api.accounting.domain.model.Invoice;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service("accountingInvoiceSettlementApplicationService")
public class InvoiceSettlementApplicationService implements ApplyInvoiceSettlementUseCase, GetPostedInvoiceSnapshotUseCase {

    private final InvoiceRepository invoiceRepository;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public InvoiceSettlementApplicationService(InvoiceRepository invoiceRepository,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.invoiceRepository = invoiceRepository;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<Invoice> apply(ApplyInvoiceSettlementCommand command) {
        return transactionalExecutor.transactional(invoiceRepository.findById(command.invoiceId())
                .switchIfEmpty(Mono.error(new InvoiceNotFoundException(command.invoiceId())))
                .map(invoice -> invoice.applySettlement(command.settlementNumber(), command.amount()))
                .flatMap(invoiceRepository::save)
                .flatMap(saved -> businessEventPublisher.publish(invoiceSettlementAppliedEvent(saved, command))
                        .thenReturn(saved)));
    }

    @Override
    public Mono<PostedInvoiceSnapshot> getPostedInvoice(java.util.UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .filter(invoice -> "POSTED".equals(invoice.status()))
                .map(invoice -> new PostedInvoiceSnapshot(
                        invoice.id(),
                        invoice.tenantId(),
                        invoice.organizationId(),
                        invoice.customerThirdPartyId(),
                        invoice.invoiceNumber(),
                        invoice.currency(),
                        invoice.status(),
                        invoice.paymentStatus(),
                        invoice.totalAmount(),
                        invoice.settledAmount(),
                        invoice.outstandingAmount()));
    }

    private BusinessEvent invoiceSettlementAppliedEvent(Invoice invoice, ApplyInvoiceSettlementCommand command) {
        return BusinessEvent.now(invoice.tenantId(), invoice.organizationId(), "INVOICE_SETTLEMENT_APPLIED", "INVOICE",
                invoice.id(), payload(
                        "invoiceNumber", invoice.invoiceNumber(),
                        "settlementNumber", command.settlementNumber(),
                        "paymentStatus", invoice.paymentStatus(),
                        "settledAmount", invoice.settledAmount(),
                        "outstandingAmount", invoice.outstandingAmount()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
