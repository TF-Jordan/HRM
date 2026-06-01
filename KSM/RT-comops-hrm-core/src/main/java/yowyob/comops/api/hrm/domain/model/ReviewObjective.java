package yowyob.comops.api.hrm.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record ReviewObjective(
        UUID id,
        UUID tenantId,
        UUID reviewId,
        String description,
        BigDecimal poids,
        BigDecimal noteAtteinte,
        String commentaire) {

    public static ReviewObjective create(UUID tenantId, UUID reviewId, String description,
                                          BigDecimal poids) {
        return new ReviewObjective(UUID.randomUUID(), tenantId, reviewId, description, poids, null, null);
    }

    public ReviewObjective evaluate(BigDecimal noteAtteinte, String commentaire) {
        return new ReviewObjective(id, tenantId, reviewId, description, poids, noteAtteinte, commentaire);
    }
}
