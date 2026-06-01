package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_social_declaration")
public record SocialDeclarationEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        String type,
        String periode,
        String format,
        String statut,
        UUID fichierId,
        Instant generatedAt,
        Instant submittedAt) implements PersistableEntity {
}
