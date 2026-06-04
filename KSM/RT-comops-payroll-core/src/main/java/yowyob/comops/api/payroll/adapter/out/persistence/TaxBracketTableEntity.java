package yowyob.comops.api.payroll.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "payroll_tax_bracket_table")
public record TaxBracketTableEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        String code,
        String label,
        String countryCode,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active) implements PersistableEntity {
}
