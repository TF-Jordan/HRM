package yowyob.comops.api.treasury.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterBankStatementRequest(@NotNull UUID organizationId, @NotNull UUID bankAccountId,
        String statementNumber, @NotNull LocalDate statementDate, @NotNull BigDecimal openingBalance,
        @NotNull BigDecimal closingBalance) {
}
