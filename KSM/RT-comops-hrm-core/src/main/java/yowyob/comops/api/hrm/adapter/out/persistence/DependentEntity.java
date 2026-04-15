package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_dependent")
public record DependentEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID employeeId,
        String nom,
        String prenom,
        LocalDate dateNaissance,
        String lienParente,
        UUID certificatFileId) implements PersistableEntity {
}
