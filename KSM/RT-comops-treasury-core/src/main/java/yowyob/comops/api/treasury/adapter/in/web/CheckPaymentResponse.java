package yowyob.comops.api.treasury.adapter.in.web;

import yowyob.comops.api.treasury.domain.model.CheckPayment;
import java.math.BigDecimal;
import java.util.UUID;

public record CheckPaymentResponse(UUID id, UUID tenantId, UUID organizationId, UUID bankAccountId, String checkNumber,
        BigDecimal amount, String beneficiary, String status) {
    public static CheckPaymentResponse from(CheckPayment checkPayment) {
        return new CheckPaymentResponse(checkPayment.id(), checkPayment.tenantId(), checkPayment.organizationId(), checkPayment.bankAccountId(),
                checkPayment.checkNumber(), checkPayment.amount(), checkPayment.beneficiary(), checkPayment.status());
    }
}
