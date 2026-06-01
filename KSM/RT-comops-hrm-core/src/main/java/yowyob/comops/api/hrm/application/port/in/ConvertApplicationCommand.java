package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Recruiter-supplied HR fields required to provision a real Employee from a
 * hired Application. The candidate's name/email/phone come from the
 * Application; everything else (matricule generation excepted) is captured
 * here at conversion time.
 */
public record ConvertApplicationCommand(
        UUID applicationId,
        UUID managerId,
        String numCnps,
        int categorie,
        String echelon,
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
