package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_loan_advance")
public record LoanAdvanceEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        UUID employeeId,
        BigDecimal montant,
        BigDecimal soldeRestant,
        BigDecimal mensualite,
        String status,
        LocalDate dateDebut,
        int nbEcheances,
        String motif,
        UUID approvedBy) implements PersistableEntity {
}
