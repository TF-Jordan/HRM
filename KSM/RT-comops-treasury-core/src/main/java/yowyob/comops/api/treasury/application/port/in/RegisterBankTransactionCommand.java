package yowyob.comops.api.treasury.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterBankTransactionCommand(
        UUID tenantId,
        UUID organizationId,
        UUID bankAccountId,
        String referenceNumber,
        String transactionType,
        LocalDate transactionDate,
        BigDecimal amount,
        String description,
        UUID createdBy) {
}
