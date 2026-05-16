package yowyob.comops.api.treasury.adapter.out.persistence;

import java.math.BigDecimal;
import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "treasury", name = "bank_statement")
public record BankStatementEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID bankAccountId,
        String statementNumber,
        LocalDate statementDate,
        BigDecimal openingBalance,
        BigDecimal closingBalance) implements PersistableEntity {
}
