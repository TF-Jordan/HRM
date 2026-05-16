package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_performance_review")
public record PerformanceReviewEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID employeeId,
        UUID evaluateurPartyId,
        String evaluateurDisplayName,
        String periode,
        BigDecimal noteGlobale,
        String commentaires,
        String planAction,
        String status) implements PersistableEntity {
}
