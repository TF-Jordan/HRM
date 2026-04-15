package yowyob.comops.api.settings.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "settings", name = "document_sequence")
public record DocumentSequenceEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        String documentType,
        String prefix,
        String suffix,
        int paddingWidth,
        long nextNumber) implements PersistableEntity {
}
