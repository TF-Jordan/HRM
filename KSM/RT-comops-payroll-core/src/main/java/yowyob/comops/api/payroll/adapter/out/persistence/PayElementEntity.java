package yowyob.comops.api.payroll.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "payroll_pay_element")
public record PayElementEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        String code,
        String label,
        String category,
        String method,
        String baseReference,
        BigDecimal rate,
        BigDecimal ceiling,
        BigDecimal floorValue,
        BigDecimal exemptionThreshold,
        BigDecimal flatAmount,
        String bracketTableCode,
        String lookupTableCode,
        boolean taxable,
        boolean socialContributable,
        String countryCode,
        int displayOrder,
        boolean active,
        LocalDate effectiveFrom,
        LocalDate effectiveTo) implements PersistableEntity {
}
