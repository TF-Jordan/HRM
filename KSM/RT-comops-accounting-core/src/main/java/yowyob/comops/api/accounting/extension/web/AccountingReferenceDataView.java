package yowyob.comops.api.accounting.extension.web;

import yowyob.comops.api.accounting.extension.web.DocumentSequenceSummaryView;
import yowyob.comops.api.accounting.extension.web.InvoiceSummaryView;
import yowyob.comops.api.accounting.extension.web.JournalSummaryView;
import yowyob.comops.api.accounting.extension.web.OpenItemSummaryView;
import yowyob.comops.api.accounting.extension.web.OrganizationSummaryView;
import yowyob.comops.api.accounting.extension.web.ThirdPartySummaryView;
import java.util.List;
import java.util.UUID;

public record AccountingReferenceDataView(
        OrganizationSummaryView currentOrganization,
        List<OrganizationSummaryView> accessibleOrganizations,
        List<JournalSummaryView> journals,
        List<DocumentSequenceSummaryView> documentSequences,
        List<InvoiceSummaryView> invoices,
        List<OpenItemSummaryView> openReceivables,
        List<OpenItemSummaryView> openPayables,
        List<ThirdPartySummaryView> customers,
        List<ThirdPartySummaryView> suppliers) {

    public ThirdPartySummaryView findThirdParty(UUID thirdPartyId) {
        if (thirdPartyId == null) {
            return null;
        }
        return customers.stream()
                .filter(thirdParty -> thirdParty.id().equals(thirdPartyId))
                .findFirst()
                .or(() -> suppliers.stream()
                        .filter(thirdParty -> thirdParty.id().equals(thirdPartyId))
                        .findFirst())
                .orElse(null);
    }
}
