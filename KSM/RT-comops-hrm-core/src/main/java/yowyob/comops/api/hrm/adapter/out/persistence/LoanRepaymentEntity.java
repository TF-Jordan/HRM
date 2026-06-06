package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_loan_repayment")
public record LoanRepaymentEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID loanId,
        UUID employeeId,
        UUID runId,
        String period,
        UUID payrollEntryId,
        BigDecimal montant,
        BigDecimal soldeApres) implements PersistableEntity {
}
