package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_contract")
public record ContractEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        UUID employeeId,
        String type,
        String position,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal salaireBase,
        BigDecimal avantagesNature,
        Integer periodeEssai,
        String status,
        String motifFin,
        UUID documentFileId) implements PersistableEntity {
}
