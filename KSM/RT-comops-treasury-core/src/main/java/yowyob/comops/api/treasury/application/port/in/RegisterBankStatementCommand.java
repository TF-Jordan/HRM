package yowyob.comops.api.treasury.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterBankStatementCommand(UUID tenantId, UUID organizationId, UUID bankAccountId, String statementNumber,
        LocalDate statementDate, BigDecimal openingBalance, BigDecimal closingBalance) {
}
