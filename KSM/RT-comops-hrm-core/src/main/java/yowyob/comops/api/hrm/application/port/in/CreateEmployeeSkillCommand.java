package yowyob.comops.api.hrm.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

public record CreateEmployeeSkillCommand(
        UUID employeeId,
        UUID skillId,
        int niveauActuel,
        int niveauAttendu,
        LocalDate dateEvaluation) {
}
