package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddContractCommand(
        String type,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal salaireBase,
        BigDecimal avantagesNature,
        Integer periodeEssai) {
}
