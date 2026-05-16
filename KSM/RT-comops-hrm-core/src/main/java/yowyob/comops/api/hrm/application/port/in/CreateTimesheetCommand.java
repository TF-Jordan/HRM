package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTimesheetCommand(
        UUID employeeId,
        String periode,
        BigDecimal heuresNormales,
        BigDecimal heuresSupplementaires,
        BigDecimal heuresNuit,
        BigDecimal heuresWeekend,
        BigDecimal absencesNonJustifiees) {
}
