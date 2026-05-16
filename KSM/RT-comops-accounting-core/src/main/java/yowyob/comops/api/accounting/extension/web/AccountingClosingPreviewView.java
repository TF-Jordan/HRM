package yowyob.comops.api.accounting.extension.web;

import yowyob.comops.api.accounting.extension.web.OrganizationSummaryView;
import java.math.BigDecimal;
import java.util.List;

public record AccountingClosingPreviewView(
        OrganizationSummaryView organization,
        int journalCount,
        int invoiceCount,
        int draftInvoiceCount,
        int postedInvoiceCount,
        BigDecimal totalReceivables,
        BigDecimal totalPayables,
        boolean ready,
        List<String> blockingIssues) {
}
