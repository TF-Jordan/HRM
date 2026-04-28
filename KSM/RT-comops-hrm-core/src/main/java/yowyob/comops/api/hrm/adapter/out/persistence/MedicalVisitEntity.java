package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_medical_visit")
public record MedicalVisitEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID employeeId,
        LocalDate dateVisite,
        String medecin,
        String resultatAptitude,
        String restrictions,
        LocalDate prochaineEcheance,
        UUID certificatFileId) implements PersistableEntity {
}
