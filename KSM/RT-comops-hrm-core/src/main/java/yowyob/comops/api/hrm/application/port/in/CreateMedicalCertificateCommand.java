package yowyob.comops.api.hrm.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

public record CreateMedicalCertificateCommand(
        UUID employeeId,
        String typeCertificat,
        LocalDate dateEmission,
        LocalDate dateExpiration,
        String statut,
        UUID fichierId) {
}
