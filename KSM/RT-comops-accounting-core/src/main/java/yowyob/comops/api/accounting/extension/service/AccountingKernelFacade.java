package yowyob.comops.api.accounting.extension.service;

import yowyob.comops.api.accounting.application.port.in.GetInvoiceUseCase;
import yowyob.comops.api.accounting.application.port.in.ListAccountingJournalsUseCase;
import yowyob.comops.api.accounting.application.port.in.ListInvoicesUseCase;
import yowyob.comops.api.accounting.application.port.in.ListOpenPayablesUseCase;
import yowyob.comops.api.accounting.application.port.in.ListOpenReceivablesUseCase;
import yowyob.comops.api.accounting.domain.model.AccountingJournal;
import yowyob.comops.api.accounting.domain.model.AccountingOpenItem;
import yowyob.comops.api.accounting.domain.model.Invoice;
import yowyob.comops.api.accounting.domain.model.InvoiceLine;
import yowyob.comops.api.accounting.extension.web.AccountingReferenceDataView;
import yowyob.comops.api.accounting.extension.web.DocumentSequenceSummaryView;
import yowyob.comops.api.accounting.extension.web.InvoiceDetailsView;
import yowyob.comops.api.accounting.extension.web.InvoiceLineSummaryView;
import yowyob.comops.api.accounting.extension.web.InvoiceSummaryView;
import yowyob.comops.api.accounting.extension.web.JournalSummaryView;
import yowyob.comops.api.accounting.extension.web.OpenItemSummaryView;
import yowyob.comops.api.accounting.extension.web.OrganizationSummaryView;
import yowyob.comops.api.accounting.extension.web.ThirdPartySummaryView;
import yowyob.comops.api.organization.application.port.in.GetOrganizationUseCase;
import yowyob.comops.api.organization.application.port.in.ListMyOrganizationsUseCase;
import yowyob.comops.api.organization.domain.model.Organization;
import yowyob.comops.api.settings.application.port.in.ListDocumentSequencesUseCase;
import yowyob.comops.api.settings.domain.model.DocumentSequence;
import yowyob.comops.api.tp.application.port.in.GetThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.ListThirdPartiesUseCase;
import yowyob.comops.api.tp.domain.model.ThirdParty;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccountingKernelFacade {

    private final GetOrganizationUseCase getOrganizationUseCase;
    private final ListMyOrganizationsUseCase listMyOrganizationsUseCase;
    private final ListAccountingJournalsUseCase listAccountingJournalsUseCase;
    private final ListDocumentSequencesUseCase listDocumentSequencesUseCase;
    private final ListInvoicesUseCase listInvoicesUseCase;
    private final ListOpenReceivablesUseCase listOpenReceivablesUseCase;
    private final ListOpenPayablesUseCase listOpenPayablesUseCase;
    private final GetInvoiceUseCase getInvoiceUseCase;
    private final ListThirdPartiesUseCase listThirdPartiesUseCase;
    private final GetThirdPartyUseCase getThirdPartyUseCase;

    public AccountingKernelFacade(GetOrganizationUseCase getOrganizationUseCase,
            ListMyOrganizationsUseCase listMyOrganizationsUseCase,
            ListAccountingJournalsUseCase listAccountingJournalsUseCase,
            ListDocumentSequencesUseCase listDocumentSequencesUseCase,
            ListInvoicesUseCase listInvoicesUseCase,
            ListOpenReceivablesUseCase listOpenReceivablesUseCase,
            ListOpenPayablesUseCase listOpenPayablesUseCase,
            GetInvoiceUseCase getInvoiceUseCase,
            ListThirdPartiesUseCase listThirdPartiesUseCase,
            GetThirdPartyUseCase getThirdPartyUseCase) {
        this.getOrganizationUseCase = getOrganizationUseCase;
        this.listMyOrganizationsUseCase = listMyOrganizationsUseCase;
        this.listAccountingJournalsUseCase = listAccountingJournalsUseCase;
        this.listDocumentSequencesUseCase = listDocumentSequencesUseCase;
        this.listInvoicesUseCase = listInvoicesUseCase;
        this.listOpenReceivablesUseCase = listOpenReceivablesUseCase;
        this.listOpenPayablesUseCase = listOpenPayablesUseCase;
        this.getInvoiceUseCase = getInvoiceUseCase;
        this.listThirdPartiesUseCase = listThirdPartiesUseCase;
        this.getThirdPartyUseCase = getThirdPartyUseCase;
    }

    public Mono<OrganizationSummaryView> getOrganization(UUID organizationId, AccountingExtensionRequestContext context) {
        return getOrganizationUseCase.getOrganization(organizationId)
                .map(this::toOrganizationSummary);
    }

    public Mono<List<OrganizationSummaryView>> listMyOrganizations(AccountingExtensionRequestContext context) {
        return listMyOrganizationsUseCase.listMine(context.requireTenantId(), context.userId())
                .map(this::toOrganizationSummary)
                .collectList();
    }

    public Flux<ThirdPartySummaryView> listCustomers(AccountingExtensionRequestContext context) {
        return listThirdPartiesUseCase.listThirdParties(context.requireOrganizationId(), "CUSTOMER", false)
                .map(this::toThirdPartySummary);
    }

    public Flux<ThirdPartySummaryView> listSuppliers(AccountingExtensionRequestContext context) {
        return listThirdPartiesUseCase.listThirdParties(context.requireOrganizationId(), "SUPPLIER", false)
                .map(this::toThirdPartySummary);
    }

    public Mono<InvoiceDetailsView> getInvoice(UUID invoiceId, AccountingExtensionRequestContext context) {
        return getInvoiceUseCase.getInvoice(invoiceId)
                .map(this::toInvoiceDetails);
    }

    public Mono<ThirdPartySummaryView> findThirdParty(UUID thirdPartyId, AccountingExtensionRequestContext context) {
        if (thirdPartyId == null) {
            return Mono.empty();
        }
        return getThirdPartyUseCase.getThirdParty(thirdPartyId)
                .map(this::toThirdPartySummary);
    }

    public Mono<AccountingReferenceDataView> loadReportingReferenceData(AccountingExtensionRequestContext context) {
        UUID tenantId = context.requireTenantId();
        UUID organizationId = context.requireOrganizationId();
        return Mono.zip(objects -> new AccountingReferenceDataView(
                        (OrganizationSummaryView) objects[0],
                        (List<OrganizationSummaryView>) objects[1],
                        (List<JournalSummaryView>) objects[2],
                        (List<DocumentSequenceSummaryView>) objects[3],
                        (List<InvoiceSummaryView>) objects[4],
                        (List<OpenItemSummaryView>) objects[5],
                        (List<OpenItemSummaryView>) objects[6],
                        (List<ThirdPartySummaryView>) objects[7],
                        (List<ThirdPartySummaryView>) objects[8]),
                getOrganization(organizationId, context),
                listMyOrganizations(context),
                listAccountingJournalsUseCase.listJournals(tenantId, organizationId).map(this::toJournalSummary).collectList(),
                listDocumentSequencesUseCase.list(tenantId, organizationId, context.agencyId()).map(this::toDocumentSequenceSummary).collectList(),
                listInvoicesUseCase.listInvoices(tenantId, organizationId).map(this::toInvoiceSummary).collectList(),
                listOpenReceivablesUseCase.listOpenReceivables(tenantId, organizationId).map(this::toOpenItemSummary).collectList(),
                listOpenPayablesUseCase.listOpenPayables(tenantId, organizationId).map(this::toOpenItemSummary).collectList(),
                listCustomers(context).collectList(),
                listSuppliers(context).collectList());
    }

    private OrganizationSummaryView toOrganizationSummary(Organization organization) {
        return new OrganizationSummaryView(
                organization.id(),
                organization.code(),
                organization.shortName(),
                organization.longName(),
                organization.status(),
                organization.isActive());
    }

    private JournalSummaryView toJournalSummary(AccountingJournal journal) {
        return new JournalSummaryView(journal.id(), journal.code(), journal.label(), journal.type(), journal.active());
    }

    private DocumentSequenceSummaryView toDocumentSequenceSummary(DocumentSequence sequence) {
        return new DocumentSequenceSummaryView(
                sequence.id(),
                sequence.tenantId(),
                sequence.organizationId(),
                sequence.agencyId(),
                sequence.documentType(),
                sequence.prefix(),
                sequence.suffix(),
                sequence.paddingWidth(),
                sequence.nextNumber());
    }

    private InvoiceSummaryView toInvoiceSummary(Invoice invoice) {
        return new InvoiceSummaryView(
                invoice.id(),
                invoice.tenantId(),
                invoice.organizationId(),
                invoice.customerThirdPartyId(),
                invoice.orderId(),
                invoice.invoiceNumber(),
                invoice.totalAmount(),
                invoice.settledAmount(),
                invoice.outstandingAmount(),
                invoice.currency(),
                invoice.status(),
                invoice.paymentStatus());
    }

    private InvoiceDetailsView toInvoiceDetails(Invoice invoice) {
        List<InvoiceLineSummaryView> lines = invoice.lines().stream().map(this::toInvoiceLineSummary).toList();
        return new InvoiceDetailsView(
                invoice.id(),
                invoice.tenantId(),
                invoice.organizationId(),
                invoice.customerThirdPartyId(),
                invoice.orderId(),
                invoice.productId(),
                invoice.invoiceNumber(),
                invoice.quantity(),
                invoice.unitPrice(),
                invoice.totalQuantity(),
                invoice.subtotalAmount(),
                invoice.totalAmount(),
                invoice.settledAmount(),
                invoice.outstandingAmount(),
                invoice.currency(),
                invoice.status(),
                invoice.paymentStatus(),
                lines);
    }

    private InvoiceLineSummaryView toInvoiceLineSummary(InvoiceLine line) {
        return new InvoiceLineSummaryView(line.productId(), line.quantity(), line.unitPrice(), line.lineAmount());
    }

    private OpenItemSummaryView toOpenItemSummary(AccountingOpenItem item) {
        return new OpenItemSummaryView(
                item.id(),
                item.organizationId(),
                item.counterpartyThirdPartyId(),
                item.reference(),
                item.balanceDue(),
                item.currency(),
                item.status(),
                item.paymentStatus(),
                item.direction());
    }

    private ThirdPartySummaryView toThirdPartySummary(ThirdParty thirdParty) {
        return new ThirdPartySummaryView(
                thirdParty.id(),
                thirdParty.code(),
                thirdParty.name(),
                thirdParty.type(),
                thirdParty.active(),
                thirdParty.accountingAccount(),
                thirdParty.accountingAccountNumbers());
    }
}
