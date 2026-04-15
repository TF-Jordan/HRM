package yowyob.comops.api.accounting.adapter.in.web;

import yowyob.comops.api.accounting.domain.model.AccountingOpenItem;
import java.math.BigDecimal;
import java.util.UUID;

public record OpenAccountingItemResponse(
        UUID id,
        UUID organizationId,
        UUID counterpartyThirdPartyId,
        String reference,
        BigDecimal balanceDue,
        String currency,
        String status,
        String paymentStatus,
        String direction) {

    public static OpenAccountingItemResponse from(AccountingOpenItem item) {
        return new OpenAccountingItemResponse(
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
}
