package yowyob.comops.api.accounting.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "accounting", name = "accounting_journal")
public record AccountingJournalEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        String code,
        String label,
        String type,
        String notes,
        boolean active) {
}
