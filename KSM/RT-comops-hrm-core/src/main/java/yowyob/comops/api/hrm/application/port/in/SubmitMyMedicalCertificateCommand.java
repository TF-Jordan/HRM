package yowyob.comops.api.hrm.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Self-service command: the calling worker submits their OWN medical certificate (e.g. sick-leave
 * justification, fitness or vaccination certificate). The employee is resolved from the caller's
 * actor — never trusted from the request — and the status is forced server-side.
 */
public record SubmitMyMedicalCertificateCommand(
        String typeCertificat,
        LocalDate dateEmission,
        LocalDate dateExpiration,
        UUID fichierId) {
}
