package yowyob.comops.api.hrm.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record PayslipLine(
        UUID id,
        UUID tenantId,
        UUID payrollEntryId,
        String libelle,
        PayslipLineType type,
        BigDecimal base,
        BigDecimal taux,
        BigDecimal montant,
        int ordreAffichage) {

    public static PayslipLine create(UUID tenantId, UUID payrollEntryId, String libelle,
                                      PayslipLineType type, BigDecimal base, BigDecimal taux,
                                      BigDecimal montant, int ordreAffichage) {
        return new PayslipLine(UUID.randomUUID(), tenantId, payrollEntryId, libelle, type,
                base, taux, montant, ordreAffichage);
    }
}
