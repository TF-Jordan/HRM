package yowyob.comops.api.treasury.adapter.in.web;

import yowyob.comops.api.treasury.domain.model.BankStatement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BankStatementResponse(UUID id, UUID tenantId, UUID organizationId, UUID bankAccountId, String statementNumber,
        LocalDate statementDate, BigDecimal openingBalance, BigDecimal closingBalance) {
    public static BankStatementResponse from(BankStatement bankStatement) {
        return new BankStatementResponse(bankStatement.id(), bankStatement.tenantId(), bankStatement.organizationId(),
                bankStatement.bankAccountId(), bankStatement.statementNumber(), bankStatement.statementDate(),
                bankStatement.openingBalance(), bankStatement.closingBalance());
    }
}
