package yowyob.comops.api.hrm.application.port.in;

import java.util.UUID;

public record UpdateEmployeeCommand(
        String numCnps,
        int categorie,
        String echelon,
        String departmentCode,
        String modePaiement,
        String compteBancaire,
        String numMobileMoney,
        String operateurMm,
        UUID managerId) {
}
