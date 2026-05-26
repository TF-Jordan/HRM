package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateEmployeeCommand(
        UUID actorId,
        String numCnps,
        int categorie,
        String echelon,
        String poste,
        LocalDate dateEmbauche,
        String departmentCode,
        String modePaiement,
        String compteBancaire,
        String numMobileMoney,
        String operateurMm,
        String contractType,
        LocalDate contractDateDebut,
        LocalDate contractDateFin,
        BigDecimal salaireBase,
        BigDecimal avantagesNature,
        Integer periodeEssai) {
}
