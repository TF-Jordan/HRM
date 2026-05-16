package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateMissionOrderCommand(
        UUID employeeId,
        String destination,
        String objet,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal montantAvance,
        String centreCout) {
}
