package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Manager's amendment of a DECLINED mission order. Creates a new mission order
 * row that references the declined one via parentOrderId, leaving the declined
 * record intact as the audit trail of the negotiation.
 */
public record AmendMissionOrderCommand(
        UUID parentMissionOrderId,
        String destination,
        String objet,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal montantAvance,
        String centreCout) {
}
