package yowyob.comops.api.hrm.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.domain.Persistable;

@Table(name = "hrm_review_objective")
public record ReviewObjectiveEntity(
        @Id UUID id,
        UUID tenantId,
        UUID reviewId,
        String description,
        BigDecimal poids,
        BigDecimal noteAtteinte,
        String commentaire) implements Persistable<UUID> {

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return true; }
}
