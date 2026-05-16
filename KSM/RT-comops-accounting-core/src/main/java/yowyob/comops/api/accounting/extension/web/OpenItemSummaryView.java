package yowyob.comops.api.accounting.extension.web;

import java.math.BigDecimal;
import java.util.UUID;

public record OpenItemSummaryView(
        UUID id,
        UUID organizationId,
        UUID counterpartyThirdPartyId,
        String reference,
        BigDecimal balanceDue,
        String currency,
        String status,
        String paymentStatus,
        String direction) {
}
