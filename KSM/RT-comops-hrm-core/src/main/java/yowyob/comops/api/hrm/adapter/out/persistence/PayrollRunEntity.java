package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_payroll_run")
public record PayrollRunEntity(
        @Id UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
        UUID organizationId, UUID agencyId, String periode, String status,
        BigDecimal totalBrut, BigDecimal totalNet, BigDecimal totalCnpsEmploye,
        BigDecimal totalCnpsEmployeur, BigDecimal totalIrpp, BigDecimal totalCac,
        BigDecimal totalCfc, int nbEmployes,
        Instant calculatedAt, UUID validatedBy, Instant validatedAt) implements PersistableEntity {
}
