package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_medical_certificate")
public record MedicalCertificateEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID employeeId,
        String typeCertificat,
        LocalDate dateEmission,
        LocalDate dateExpiration,
        String statut,
        UUID fichierId) implements PersistableEntity {
}
