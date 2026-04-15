package yowyob.comops.api.accounting.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountingOpenItem(
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
