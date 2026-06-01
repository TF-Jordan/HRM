package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_employee_personal_info")
public record EmployeePersonalInfoEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID employeeId,
        String lieuNaissance,
        String situationMatrimoniale,
        String typePiece,
        String numeroPiece,
        LocalDate dateEmissionPiece,
        String niuFiscal,
        String permisConduire,
        String languesParlees,
        String emailPersonnel,
        String telephoneDomicile,
        String whatsapp,
        String adressePostale,
        String adresseDomicile,
        String ville,
        String region,
        String codePostal) implements PersistableEntity {
}
