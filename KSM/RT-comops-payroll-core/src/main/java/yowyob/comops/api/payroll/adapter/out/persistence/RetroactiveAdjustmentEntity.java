package yowyob.comops.api.payroll.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "payroll_retroactive_adjustment")
public record RetroactiveAdjustmentEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID employeeId,
        String originPeriod,
        String targetPeriod,
        String reason,
        String currency,
        BigDecimal oldGross,
        BigDecimal newGross,
        BigDecimal deltaGross,
        BigDecimal oldNet,
        BigDecimal newNet,
        BigDecimal deltaNet,
        String status) implements PersistableEntity {
}
