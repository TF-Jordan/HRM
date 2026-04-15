package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_leave_request")
public record LeaveRequestEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        UUID employeeId,
        String type,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal nbJours,
        String status,
        String motif,
        UUID valideurPartyId,
        String valideurDisplayName,
        Instant dateValidation,
        String commentaireValideur,
        UUID justificatifFileId) implements PersistableEntity {
}
