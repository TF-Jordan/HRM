package yowyob.comops.api.treasury.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record RegisterCheckPaymentCommand(UUID tenantId, UUID organizationId, UUID bankAccountId, String checkNumber,
        BigDecimal amount, String beneficiary) {
}
