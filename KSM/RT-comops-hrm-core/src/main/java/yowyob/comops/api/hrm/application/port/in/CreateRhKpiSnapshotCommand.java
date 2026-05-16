package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateRhKpiSnapshotCommand(
        UUID organizationId,
        String periode,
        int effectifTotal,
        int effectifActif,
        BigDecimal tauxTurnover,
        BigDecimal tauxAbsenteisme,
        BigDecimal masseSalariale,
        BigDecimal couvertureCompetences) {
}
