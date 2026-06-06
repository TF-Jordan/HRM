package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_training_request")
public record TrainingRequestEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID employeeId,
        UUID trainingId,
        String motivation,
        String status,
        String decisionReason,
        UUID enrollmentId,
        Instant decidedAt) implements PersistableEntity {
}
