package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_employee")
public record EmployeeEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        UUID actorId,
        UUID managerId,
        String matricule,
        String numCnps,
        int categorie,
        String echelon,
        LocalDate dateEmbauche,
        String status,
        String departmentCode,
        String modePaiement,
        String compteBancaire,
        String numMobileMoney,
        String operateurMm,
        String actorDisplayName) implements PersistableEntity {
}
