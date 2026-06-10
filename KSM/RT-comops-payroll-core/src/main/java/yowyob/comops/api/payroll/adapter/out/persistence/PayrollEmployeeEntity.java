package yowyob.comops.api.payroll.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "payroll_employee")
public record PayrollEmployeeEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        UUID actorId,
        String matricule,
        String displayName,
        String email,
        String socialSecurityNo,
        int categorie,
        String echelon,
        String departmentCode,
        LocalDate hireDate,
        LocalDate departureDate,
        String maritalStatus,
        int dependentChildren,
        BigDecimal baseSalary,
        BigDecimal benefitsInKind,
        String position,
        String paymentChannel,
        String accountRef,
        boolean active) implements PersistableEntity {
}
