package yowyob.comops.api.payroll.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "payroll_garnishment_order")
public record GarnishmentOrderEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID employeeId,
        String type,
        String beneficiary,
        String reference,
        BigDecimal totalAmount,
        BigDecimal remainingBalance,
        BigDecimal monthlyAmount,
        String status) implements PersistableEntity {
}
