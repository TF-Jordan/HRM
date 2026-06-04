package yowyob.comops.api.payroll.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "payroll_entry")
public record PayrollEntryEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID payrollRunId,
        UUID employeeId,
        String currency,
        BigDecimal salaireBase,
        BigDecimal brut,
        BigDecimal totalDeductions,
        BigDecimal incomeTax,
        BigDecimal employerCharges,
        BigDecimal net,
        String paymentStatus,
        String paymentChannel,
        String accountRef) implements PersistableEntity {
}
