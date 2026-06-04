package yowyob.comops.api.payroll.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "payroll_annual_accumulator")
public record AnnualAccumulatorEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID employeeId,
        int year,
        BigDecimal cumulativeGross,
        BigDecimal cumulativeDeductions,
        BigDecimal cumulativeIncomeTax,
        BigDecimal cumulativeNet,
        BigDecimal cumulativeEmployerCharges) implements PersistableEntity {
}
