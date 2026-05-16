package yowyob.comops.api.hrm.application.port.in;

import java.time.LocalDate;

public record AddDependentCommand(
        String nom,
        String prenom,
        LocalDate dateNaissance,
        String lienParente) {
}
