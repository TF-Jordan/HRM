package yowyob.comops.api.hrm.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseLine(
        UUID id,
        UUID tenantId,
        UUID expenseReportId,
        String description,
        BigDecimal montant,
        String categorie,
        UUID justificatifFileId) {

    public static ExpenseLine create(UUID tenantId, UUID expenseReportId, String description,
                                      BigDecimal montant, String categorie, UUID justificatifFileId) {
        return new ExpenseLine(UUID.randomUUID(), tenantId, expenseReportId, description,
                montant, categorie, justificatifFileId);
    }
}
