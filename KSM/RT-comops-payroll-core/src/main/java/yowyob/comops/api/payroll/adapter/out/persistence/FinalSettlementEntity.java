package yowyob.comops.api.payroll.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "payroll_final_settlement")
public record FinalSettlementEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID employeeId,
        String periode,
        LocalDate departureDate,
        String reason,
        String currency,
        int seniorityYears,
        BigDecimal proratedSalary,
        BigDecimal leaveCompensation,
        BigDecimal noticeIndemnity,
        BigDecimal severanceIndemnity,
        BigDecimal gratification,
        BigDecimal grossSettlement,
        BigDecimal loanDeducted,
        BigDecimal netSettlement,
        String status) implements PersistableEntity {
}
