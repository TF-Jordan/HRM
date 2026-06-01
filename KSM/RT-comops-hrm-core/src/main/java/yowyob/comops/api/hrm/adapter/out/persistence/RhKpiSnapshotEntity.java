package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_rh_kpi_snapshot")
public record RhKpiSnapshotEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        String periode,
        int effectifTotal,
        int effectifActif,
        BigDecimal tauxTurnover,
        BigDecimal tauxAbsenteisme,
        BigDecimal masseSalariale,
        BigDecimal couvertureCompetences) implements PersistableEntity {
}
