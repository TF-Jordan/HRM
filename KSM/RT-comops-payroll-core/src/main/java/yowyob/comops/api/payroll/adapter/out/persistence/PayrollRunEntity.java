package yowyob.comops.api.payroll.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "payroll_run")
public record PayrollRunEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        String periode,
        String runType,
        String status,
        String currency,
        BigDecimal totalGross,
        BigDecimal totalEmployeeDeductions,
        BigDecimal totalIncomeTax,
        BigDecimal totalNet,
        BigDecimal totalEmployerCharges,
        int nbEmployes,
        Instant calculatedAt,
        UUID validatedBy,
        Instant validatedAt,
        UUID approvedBy,
        Instant approvedAt,
        Instant paidAt,
        Instant closedAt,
        String rejectionReason,
        UUID rejectedBy,
        Instant rejectedAt) implements PersistableEntity {
}
