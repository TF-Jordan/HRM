package yowyob.comops.api.payroll.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Child row of a {@code payroll_tax_bracket_table}. Brackets are replaced wholesale on save
 * (delete-by-table then insert), so the entity always reports {@link #isNew()} = true to force
 * an INSERT despite carrying a pre-assigned id.
 */
@Table(name = "payroll_tax_bracket")
public record TaxBracketEntity(
        @Id UUID id,
        UUID tenantId,
        UUID tableId,
        int ordre,
        BigDecimal lowerBound,
        BigDecimal upperBound,
        BigDecimal rate) implements Persistable<UUID> {

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
