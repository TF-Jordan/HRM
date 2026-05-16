package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record RequestLoanAdvanceCommand(
        UUID employeeId,
        BigDecimal montant,
        int nbEcheances,
        String motif) {
}
