package yowyob.comops.api.hrm.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.domain.Persistable;

@Table(name = "hrm_payslip_line")
public record PayslipLineEntity(
        @Id UUID id, UUID tenantId, UUID payrollEntryId,
        String libelle, String type, BigDecimal base, BigDecimal taux,
        BigDecimal montant, int ordreAffichage) implements Persistable<UUID> {

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return true; }
}
