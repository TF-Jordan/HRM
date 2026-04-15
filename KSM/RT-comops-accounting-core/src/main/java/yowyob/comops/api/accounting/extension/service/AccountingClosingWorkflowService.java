package yowyob.comops.api.accounting.extension.service;

import yowyob.comops.api.accounting.extension.web.AccountingClosingPreviewView;
import yowyob.comops.api.accounting.extension.web.AccountingReferenceDataView;
import yowyob.comops.api.accounting.extension.web.DocumentSequenceSummaryView;
import yowyob.comops.api.accounting.extension.web.InvoiceSummaryView;
import yowyob.comops.api.accounting.extension.web.OpenItemSummaryView;
import yowyob.comops.api.accounting.extension.service.AccountingExtensionRequestContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AccountingClosingWorkflowService {

    private final AccountingKernelFacade accountingKernelFacade;

    public AccountingClosingWorkflowService(AccountingKernelFacade accountingKernelFacade) {
        this.accountingKernelFacade = accountingKernelFacade;
    }

    public Mono<AccountingClosingPreviewView> preview(AccountingExtensionRequestContext context) {
        return accountingKernelFacade.loadReportingReferenceData(context)
                .map(this::buildPreview);
    }

    private AccountingClosingPreviewView buildPreview(AccountingReferenceDataView referenceData) {
        List<String> blockingIssues = new ArrayList<>();
        if (referenceData.journals().isEmpty()) {
            blockingIssues.add("NO_ACCOUNTING_JOURNALS");
        }
        if (referenceData.documentSequences().stream().noneMatch(this::isSalesInvoiceSequence)) {
            blockingIssues.add("MISSING_SALES_INVOICE_SEQUENCE");
        }

        int draftInvoiceCount = (int) referenceData.invoices().stream()
                .filter(invoice -> "DRAFT".equals(invoice.status()))
                .count();
        if (draftInvoiceCount > 0) {
            blockingIssues.add("DRAFT_INVOICES_PENDING");
        }

        int postedInvoiceCount = (int) referenceData.invoices().stream()
                .filter(invoice -> "POSTED".equals(invoice.status()))
                .count();

        BigDecimal totalReceivables = sum(referenceData.openReceivables());
        BigDecimal totalPayables = sum(referenceData.openPayables());

        return new AccountingClosingPreviewView(
                referenceData.currentOrganization(),
                referenceData.journals().size(),
                referenceData.invoices().size(),
                draftInvoiceCount,
                postedInvoiceCount,
                totalReceivables,
                totalPayables,
                blockingIssues.isEmpty(),
                List.copyOf(blockingIssues));
    }

    private BigDecimal sum(List<OpenItemSummaryView> items) {
        return items.stream()
                .map(OpenItemSummaryView::balanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isSalesInvoiceSequence(DocumentSequenceSummaryView sequence) {
        return "SALES_INVOICE".equals(sequence.documentType());
    }
}
