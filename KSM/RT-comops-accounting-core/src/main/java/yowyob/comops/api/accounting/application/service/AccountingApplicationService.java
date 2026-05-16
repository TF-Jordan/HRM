package yowyob.comops.api.accounting.application.service;

import yowyob.comops.api.accounting.application.port.in.CreateInvoiceCommand;
import yowyob.comops.api.accounting.application.port.in.CreateInvoiceFromSalesOrderUseCase;
import yowyob.comops.api.accounting.application.port.in.CreateInvoiceLineCommand;
import yowyob.comops.api.accounting.application.port.in.CreateInvoiceUseCase;
import yowyob.comops.api.accounting.application.port.in.DeleteInvoiceUseCase;
import yowyob.comops.api.accounting.application.port.in.GetInvoiceUseCase;
import yowyob.comops.api.accounting.application.port.in.ListAccountingJournalsUseCase;
import yowyob.comops.api.accounting.application.port.in.ListInvoicesUseCase;
import yowyob.comops.api.accounting.application.port.in.ListOpenPayablesUseCase;
import yowyob.comops.api.accounting.application.port.in.ListOpenReceivablesUseCase;
import yowyob.comops.api.accounting.application.port.in.UpdateInvoiceCommand;
import yowyob.comops.api.accounting.application.port.in.UpdateInvoiceUseCase;
import yowyob.comops.api.accounting.application.port.out.ConfirmedSalesOrderLineSnapshot;
import yowyob.comops.api.accounting.application.port.out.ConfirmedSalesOrderProvider;
import yowyob.comops.api.accounting.application.port.out.ConfirmedSalesOrderSnapshot;
import yowyob.comops.api.accounting.application.port.out.AccountingJournalRepository;
import yowyob.comops.api.accounting.application.port.out.InvoiceRepository;
import yowyob.comops.api.accounting.application.port.out.SupplierExposureProvider;
import yowyob.comops.api.accounting.application.port.out.SupplierExposureSnapshot;
import yowyob.comops.api.accounting.domain.DuplicateInvoiceNumberException;
import yowyob.comops.api.accounting.domain.InvoiceAlreadyExistsForOrderException;
import yowyob.comops.api.accounting.domain.InvoiceNotFoundException;
import yowyob.comops.api.accounting.domain.SalesOrderInvoiceSourceNotFoundException;
import yowyob.comops.api.accounting.domain.model.AccountingJournal;
import yowyob.comops.api.accounting.domain.model.AccountingOpenItem;
import yowyob.comops.api.accounting.domain.model.Invoice;
import yowyob.comops.api.accounting.domain.model.InvoiceLine;
import yowyob.comops.api.common.domain.model.DocumentTypes;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.product.application.port.out.ProductRepository;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberCommand;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberUseCase;
import yowyob.comops.api.tp.application.port.out.ThirdPartyRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccountingApplicationService implements CreateInvoiceUseCase, CreateInvoiceFromSalesOrderUseCase,
        GetInvoiceUseCase, ListInvoicesUseCase, ListAccountingJournalsUseCase, ListOpenReceivablesUseCase, ListOpenPayablesUseCase,
        UpdateInvoiceUseCase, DeleteInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final AccountingJournalRepository accountingJournalRepository;
    private final SupplierExposureProvider supplierExposureProvider;
    private final ProductRepository productRepository;
    private final ThirdPartyRepository thirdPartyRepository;
    private final GenerateDocumentNumberUseCase generateDocumentNumberUseCase;
    private final ConfirmedSalesOrderProvider confirmedSalesOrderProvider;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public AccountingApplicationService(InvoiceRepository invoiceRepository,
            AccountingJournalRepository accountingJournalRepository,
            SupplierExposureProvider supplierExposureProvider,
            ProductRepository productRepository,
            ThirdPartyRepository thirdPartyRepository,
            GenerateDocumentNumberUseCase generateDocumentNumberUseCase,
            ConfirmedSalesOrderProvider confirmedSalesOrderProvider,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.invoiceRepository = invoiceRepository;
        this.accountingJournalRepository = accountingJournalRepository;
        this.supplierExposureProvider = supplierExposureProvider;
        this.productRepository = productRepository;
        this.thirdPartyRepository = thirdPartyRepository;
        this.generateDocumentNumberUseCase = generateDocumentNumberUseCase;
        this.confirmedSalesOrderProvider = confirmedSalesOrderProvider;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<Invoice> createInvoice(CreateInvoiceCommand command) {
        Objects.requireNonNull(command, "command is required");
        List<InvoiceLine> lines = normalizeLines(command.lines());
        return transactionalExecutor.transactional(validateInvoiceReferences(command.tenantId(), command.organizationId(),
                        command.customerThirdPartyId(), lines)
                .then(resolveInvoiceNumber(command.tenantId(), command.organizationId(),
                        command.invoiceNumber())
                .map(invoiceNumber -> Invoice.create(command.tenantId(), command.organizationId(),
                        command.customerThirdPartyId(), command.orderId(), invoiceNumber, lines, command.currency()))
                .flatMap(this::ensureInvoiceCanBeSaved)
                .flatMap(saved -> businessEventPublisher.publish(invoiceCreatedEvent(saved, "MANUAL")).thenReturn(saved))));
    }

    @Override
    public Mono<Invoice> createFromSalesOrder(UUID tenantId, UUID orderId) {
        return transactionalExecutor.transactional(confirmedSalesOrderProvider.getConfirmedOrder(tenantId, orderId)
                .switchIfEmpty(Mono.error(new SalesOrderInvoiceSourceNotFoundException(orderId)))
                .flatMap(snapshot -> invoiceRepository.existsByOrderId(orderId)
                        .flatMap(exists -> exists
                                ? Mono.error(new InvoiceAlreadyExistsForOrderException(orderId))
                                : createInvoiceFromSnapshot(snapshot))));
    }

    @Override
    public Mono<Invoice> getInvoice(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .switchIfEmpty(Mono.error(new InvoiceNotFoundException(invoiceId)));
    }

    @Override
    public Flux<Invoice> listInvoices(UUID tenantId, UUID organizationId) {
        return invoiceRepository.findByOrganizationId(tenantId, organizationId);
    }

    @Override
    public Flux<AccountingJournal> listJournals(UUID tenantId, UUID organizationId) {
        return accountingJournalRepository.findByOrganizationId(tenantId, organizationId)
                .collectList()
                .flatMapMany(existing -> existing.isEmpty()
                        ? Flux.fromIterable(defaultJournals(tenantId, organizationId))
                                .flatMap(accountingJournalRepository::save)
                        : Flux.fromIterable(existing));
    }

    @Override
    public Flux<AccountingOpenItem> listOpenReceivables(UUID tenantId, UUID organizationId) {
        return invoiceRepository.findByOrganizationId(tenantId, organizationId)
                .filter(invoice -> "POSTED".equals(invoice.status()))
                .filter(invoice -> invoice.outstandingAmount().signum() > 0)
                .map(this::toReceivableOpenItem);
    }

    @Override
    public Flux<AccountingOpenItem> listOpenPayables(UUID tenantId, UUID organizationId) {
        return supplierExposureProvider.listSupplierExposures(tenantId, organizationId)
                .filter(exposure -> exposure.balanceDue() != null)
                .filter(exposure -> exposure.balanceDue().signum() > 0)
                .map(this::toPayableOpenItem);
    }

    @Override
    public Mono<Invoice> updateInvoice(UpdateInvoiceCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(invoiceRepository.findById(command.invoiceId())
                .switchIfEmpty(Mono.error(new InvoiceNotFoundException(command.invoiceId())))
                .flatMap(existing -> {
                    if (!existing.organizationId().equals(command.organizationId())) {
                        return Mono.error(new IllegalArgumentException("invoice does not belong to the provided organization"));
                    }
                    Invoice updated = existing.update(command.organizationId(), command.customerThirdPartyId(),
                            command.orderId(), command.invoiceNumber(), normalizeLines(command.lines()),
                            command.currency());
                    return validateInvoiceReferences(updated.tenantId(), updated.organizationId(),
                                    updated.customerThirdPartyId(), updated.lines())
                            .then(invoiceRepository.existsByInvoiceNumberExcludingId(updated.tenantId(), updated.organizationId(),
                                    updated.invoiceNumber(), updated.id())
                            .flatMap(exists -> exists
                                    ? Mono.error(new DuplicateInvoiceNumberException(updated.invoiceNumber()))
                                    : invoiceRepository.save(updated)));
                }));
    }

    @Override
    public Mono<Void> deleteInvoice(UUID invoiceId) {
        return transactionalExecutor.transactional(invoiceRepository.findById(invoiceId)
                .switchIfEmpty(Mono.error(new InvoiceNotFoundException(invoiceId)))
                .flatMap(existing -> {
                    if (!"DRAFT".equals(existing.status())) {
                        return Mono.error(new IllegalArgumentException("only DRAFT invoices can be deleted"));
                    }
                    return invoiceRepository.deleteById(existing.id());
                }));
    }

    private Mono<Invoice> createInvoiceFromSnapshot(ConfirmedSalesOrderSnapshot snapshot) {
        List<InvoiceLine> lines = snapshot.lines().stream()
                .map(this::toInvoiceLine)
                .toList();
        return validateInvoiceReferences(snapshot.tenantId(), snapshot.organizationId(),
                        snapshot.customerThirdPartyId(), lines)
                .then(resolveInvoiceNumber(snapshot.tenantId(), snapshot.organizationId(), null))
                .map(invoiceNumber -> Invoice.create(snapshot.tenantId(), snapshot.organizationId(),
                        snapshot.customerThirdPartyId(), snapshot.orderId(), invoiceNumber, lines, snapshot.currency()))
                .flatMap(this::ensureInvoiceCanBeSaved)
                .flatMap(saved -> businessEventPublisher.publish(invoiceCreatedEvent(saved, "SALES_ORDER")).thenReturn(saved));
    }

    private Mono<Void> validateInvoiceReferences(UUID tenantId, UUID organizationId, UUID customerThirdPartyId,
            List<InvoiceLine> lines) {
        return Mono.when(
                validateCustomer(tenantId, organizationId, customerThirdPartyId),
                validateProducts(tenantId, organizationId, lines));
    }

    private Mono<Void> validateCustomer(UUID tenantId, UUID organizationId, UUID customerThirdPartyId) {
        return thirdPartyRepository.findById(tenantId, customerThirdPartyId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("customer third party not found")))
                .flatMap(thirdParty -> {
                    if (!thirdParty.organizationId().equals(organizationId)) {
                        return Mono.error(new IllegalArgumentException("customer third party does not belong to the organization"));
                    }
                    if (!thirdParty.active()) {
                        return Mono.error(new IllegalArgumentException("customer third party is not active"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> validateProducts(UUID tenantId, UUID organizationId, List<InvoiceLine> lines) {
        return Flux.fromIterable(lines)
                .map(InvoiceLine::productId)
                .distinct()
                .flatMap(productId -> productRepository.findById(tenantId, productId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("product not found: " + productId)))
                        .flatMap(product -> {
                            if (!product.organizationId().equals(organizationId)) {
                                return Mono.error(new IllegalArgumentException("product does not belong to the organization: " + productId));
                            }
                            if (!product.active()) {
                                return Mono.error(new IllegalArgumentException("product is not active: " + productId));
                            }
                            return Mono.empty();
                        }))
                .then();
    }

    private Mono<Invoice> ensureInvoiceCanBeSaved(Invoice invoice) {
        return invoiceRepository.existsByInvoiceNumber(invoice.tenantId(), invoice.organizationId(), invoice.invoiceNumber())
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateInvoiceNumberException(invoice.invoiceNumber()))
                        : invoiceRepository.save(invoice));
    }

    private List<InvoiceLine> normalizeLines(List<CreateInvoiceLineCommand> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("at least one invoice line is required");
        }
        return lines.stream()
                .map(this::toInvoiceLine)
                .toList();
    }

    private InvoiceLine toInvoiceLine(CreateInvoiceLineCommand line) {
        Objects.requireNonNull(line, "invoice line is required");
        return InvoiceLine.create(line.productId(), line.quantity(), line.unitPrice());
    }

    private InvoiceLine toInvoiceLine(ConfirmedSalesOrderLineSnapshot line) {
        Objects.requireNonNull(line, "sales order line snapshot is required");
        return InvoiceLine.create(line.productId(), line.quantity(), line.unitPrice());
    }

    private Mono<String> resolveInvoiceNumber(UUID tenantId, UUID organizationId, String requestedInvoiceNumber) {
        if (requestedInvoiceNumber != null && !requestedInvoiceNumber.isBlank()) {
            return Mono.just(requestedInvoiceNumber.trim());
        }
        return generateDocumentNumberUseCase.generate(new GenerateDocumentNumberCommand(tenantId,
                organizationId, null, DocumentTypes.SALES_INVOICE));
    }

    private BusinessEvent invoiceCreatedEvent(Invoice invoice, String source) {
        return BusinessEvent.now(invoice.tenantId(), invoice.organizationId(), "INVOICE_CREATED", "INVOICE",
                invoice.id(), payload(
                        "invoiceNumber", invoice.invoiceNumber(),
                        "customerThirdPartyId", invoice.customerThirdPartyId(),
                        "orderId", invoice.orderId(),
                        "status", invoice.status(),
                        "paymentStatus", invoice.paymentStatus(),
                        "currency", invoice.currency(),
                        "lineCount", invoice.lines().size(),
                        "totalQuantity", invoice.totalQuantity(),
                        "totalAmount", invoice.totalAmount(),
                        "source", source));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }

    private java.util.List<AccountingJournal> defaultJournals(UUID tenantId, UUID organizationId) {
        return java.util.List.of(
                AccountingJournal.create(tenantId, organizationId, "VEN", "Journal des ventes", "SALES",
                        "Default sales journal", true),
                AccountingJournal.create(tenantId, organizationId, "ACH", "Journal des achats", "PURCHASE",
                        "Default purchase journal", true),
                AccountingJournal.create(tenantId, organizationId, "BAN", "Journal de banque", "BANK",
                        "Default bank journal", true),
                AccountingJournal.create(tenantId, organizationId, "CAI", "Journal de caisse", "CASH",
                        "Default cash journal", true));
    }

    private AccountingOpenItem toPayableOpenItem(SupplierExposureSnapshot exposure) {
        return new AccountingOpenItem(
                UUID.nameUUIDFromBytes(("supplier-payable:" + exposure.thirdPartyId()).getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                exposure.organizationId(),
                exposure.thirdPartyId(),
                exposure.reference(),
                exposure.balanceDue(),
                exposure.currency(),
                "OPEN",
                "UNPAID",
                "PAYABLE");
    }

    private AccountingOpenItem toReceivableOpenItem(Invoice invoice) {
        return new AccountingOpenItem(
                invoice.id(),
                invoice.organizationId(),
                invoice.customerThirdPartyId(),
                invoice.invoiceNumber(),
                invoice.outstandingAmount(),
                invoice.currency(),
                invoice.status(),
                invoice.paymentStatus(),
                "RECEIVABLE");
    }
}
