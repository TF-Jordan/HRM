package yowyob.comops.api.hrm.application.port.in;

public record UpdateEmployeeCommand(
        String numCnps,
        int categorie,
        String echelon,
        String poste,
        String departmentCode,
        String modePaiement,
        String compteBancaire,
        String numMobileMoney,
        String operateurMm) {
}
