package yowyob.comops.api.treasury.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("treasury.transaction_type")
public record LegacyTransactionTypeEntity(
        @Id UUID id,
        @Column("organization_id") UUID organizationId,
        String code,
        String label,
        boolean inbound,
        @Column("created_at") Instant createdAt) {
}
