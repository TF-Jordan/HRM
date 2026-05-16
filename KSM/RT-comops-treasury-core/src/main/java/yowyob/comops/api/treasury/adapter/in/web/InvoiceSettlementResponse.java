package yowyob.comops.api.treasury.adapter.in.web;

import yowyob.comops.api.treasury.domain.model.InvoiceSettlement;
import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceSettlementResponse(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        UUID bankAccountId,
        UUID invoiceId,
        String settlementNumber,
        String paymentMethod,
        BigDecimal amount,
        String currency,
        String status) {

    public static InvoiceSettlementResponse from(InvoiceSettlement settlement) {
        return new InvoiceSettlementResponse(settlement.id(), settlement.tenantId(), settlement.organizationId(),
                settlement.bankAccountId(), settlement.invoiceId(), settlement.settlementNumber(),
                settlement.paymentMethod(), settlement.amount(), settlement.currency(), settlement.status());
    }
}
