package yowyob.comops.api.payroll.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Child row of a {@code payroll_lookup_table}. Like tax brackets, entries are replaced wholesale
 * on save, so the entity always reports {@link #isNew()} = true to force an INSERT.
 */
@Table(name = "payroll_lookup_entry")
public record LookupEntryEntity(
        @Id UUID id,
        UUID tenantId,
        UUID tableId,
        int ordre,
        BigDecimal lowerBound,
        BigDecimal upperBound,
        BigDecimal amount) implements Persistable<UUID> {

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
