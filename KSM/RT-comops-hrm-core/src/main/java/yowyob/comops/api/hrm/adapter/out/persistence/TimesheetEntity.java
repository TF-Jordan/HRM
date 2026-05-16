package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_timesheet")
public record TimesheetEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        UUID employeeId,
        String periode,
        BigDecimal heuresNormales,
        BigDecimal heuresSupplementaires,
        BigDecimal heuresNuit,
        BigDecimal heuresWeekend,
        BigDecimal absencesNonJustifiees,
        String status) implements PersistableEntity {
}
