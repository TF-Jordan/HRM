package yowyob.comops.api.treasury.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("treasury.bank")
public record LegacyBankEntity(
        @Id UUID id,
        @Column("organization_id") UUID organizationId,
        String code,
        String name,
        boolean active,
        @Column("created_at") Instant createdAt) {
}
