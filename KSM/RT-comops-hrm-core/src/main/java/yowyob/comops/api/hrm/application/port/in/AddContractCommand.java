package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.UUID;

public record AddContractCommand(
        String type,
        String position,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal salaireBase,
        BigDecimal avantagesNature,
        Integer periodeEssai,
        UUID documentFileId) {
}
