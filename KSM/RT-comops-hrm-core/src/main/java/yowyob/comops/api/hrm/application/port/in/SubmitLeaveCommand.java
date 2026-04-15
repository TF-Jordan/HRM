package yowyob.comops.api.hrm.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

public record SubmitLeaveCommand(
        UUID employeeId,
        String type,
        LocalDate dateDebut,
        LocalDate dateFin,
        String motif,
        UUID justificatifFileId) {
}
