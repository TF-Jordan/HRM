package yowyob.comops.api.hrm.application.port.in;

import java.time.LocalDate;

public record UpsertPersonalInfoCommand(
        String lieuNaissance,
        String situationMatrimoniale,
        String typePiece,
        String numeroPiece,
        LocalDate dateEmissionPiece,
        String niuFiscal,
        String permisConduire,
        String languesParlees,
        String emailPersonnel,
        String telephoneDomicile,
        String whatsapp,
        String adressePostale,
        String adresseDomicile,
        String ville,
        String region,
        String codePostal) {
}
