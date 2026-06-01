package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_interview")
public record InterviewEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID applicationId,
        String type,
        Instant dateHeure,
        String lieu,
        UUID interviewerPartyId,
        String interviewerDisplayName,
        String notes,
        String resultat) implements PersistableEntity {
}
