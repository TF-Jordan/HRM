package yowyob.comops.api.payroll.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "payroll_payslip_line")
public record PayslipLineEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID payrollEntryId,
        String payElementCode,
        String libelle,
        String type,
        BigDecimal base,
        BigDecimal taux,
        BigDecimal montant,
        int ordreAffichage) implements PersistableEntity {
}
