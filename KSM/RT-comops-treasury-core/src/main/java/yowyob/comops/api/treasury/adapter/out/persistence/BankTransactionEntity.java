package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "treasury", name = "bank_transaction")
public record BankTransactionEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID bankAccountId,
        UUID statementId,
        String referenceNumber,
        String transactionType,
        LocalDate transactionDate,
        BigDecimal amount,
        String description,
        UUID createdBy,
        String status,
        Instant reconciledAt) implements PersistableEntity {
}
