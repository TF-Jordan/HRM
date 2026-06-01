package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record AddExpenseLineCommand(
        UUID expenseReportId,
        String description,
        BigDecimal montant,
        String categorie,
        UUID justificatifFileId) {
}
