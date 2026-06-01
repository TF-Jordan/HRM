package yowyob.comops.api.cashier.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cashier.cash_document")
public record CashDocumentEntity(
        @Id UUID id,
        @Column("organization_id") UUID organizationId,
        String type,
        @Column("target_id") UUID targetId,
        String reference,
        @Column("created_at") Instant createdAt) {
}
