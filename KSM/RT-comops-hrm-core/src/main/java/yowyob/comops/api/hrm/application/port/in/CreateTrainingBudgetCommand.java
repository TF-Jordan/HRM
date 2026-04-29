package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTrainingBudgetCommand(
        UUID organizationId,
        UUID agencyId,
        int annee,
        BigDecimal montantAlloue) {
}
