package yowyob.comops.api.hrm.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.domain.Persistable;

@Table(name = "hrm_expense_line")
public record ExpenseLineEntity(
        @Id UUID id, UUID tenantId, UUID expenseReportId,
        String description, BigDecimal montant, String categorie,
        UUID justificatifFileId) implements Persistable<UUID> {

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return true; }
}
