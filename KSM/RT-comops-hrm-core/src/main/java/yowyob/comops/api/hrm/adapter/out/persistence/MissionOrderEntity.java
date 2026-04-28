package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_mission_order")
public record MissionOrderEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID employeeId,
        String destination,
        String objet,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal montantAvance,
        String centreCout,
        String status) implements PersistableEntity {
}
