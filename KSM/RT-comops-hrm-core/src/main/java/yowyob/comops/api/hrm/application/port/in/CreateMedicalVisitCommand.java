package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.AptitudeResult;

import java.time.LocalDate;
import java.util.UUID;

public record CreateMedicalVisitCommand(
        UUID employeeId,
        LocalDate dateVisite,
        String medecin,
        AptitudeResult resultatAptitude,
        String restrictions,
        LocalDate prochaineEcheance,
        UUID certificatFileId) {
}
