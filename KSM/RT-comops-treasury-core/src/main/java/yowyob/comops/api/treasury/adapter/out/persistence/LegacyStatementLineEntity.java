package yowyob.comops.api.treasury.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("treasury.statement_line")
public record LegacyStatementLineEntity(
        @Id UUID id,
        @Column("statement_id") UUID statementId,
        String reference,
        BigDecimal amount,
        String currency,
        String direction,
        String status,
        boolean reconciled,
        @Column("created_at") Instant createdAt) {
}
