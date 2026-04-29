package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class TrainingBudget extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final int annee;
    private final BigDecimal montantAlloue;
    private final BigDecimal montantEngage;
    private final BigDecimal montantRealise;

    private TrainingBudget(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                           UUID organizationId, UUID agencyId, int annee,
                           BigDecimal montantAlloue, BigDecimal montantEngage, BigDecimal montantRealise) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.annee = annee;
        this.montantAlloue = Objects.requireNonNull(montantAlloue);
        this.montantEngage = Objects.requireNonNull(montantEngage);
        this.montantRealise = Objects.requireNonNull(montantRealise);
        this.agencyId = agencyId;
    }

    public static TrainingBudget create(UUID tenantId, UUID organizationId, UUID agencyId,
                                         int annee, BigDecimal montantAlloue) {
        Instant now = Instant.now();
        return new TrainingBudget(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId,
                annee, montantAlloue, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static TrainingBudget rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                            UUID organizationId, UUID agencyId, int annee,
                                            BigDecimal montantAlloue, BigDecimal montantEngage,
                                            BigDecimal montantRealise) {
        return new TrainingBudget(id, tenantId, createdAt, updatedAt, organizationId, agencyId,
                annee, montantAlloue, montantEngage, montantRealise);
    }

    public TrainingBudget engage(BigDecimal montant) {
        return new TrainingBudget(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                annee, montantAlloue, montantEngage.add(montant), montantRealise);
    }

    public TrainingBudget realiser(BigDecimal montant) {
        return new TrainingBudget(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                annee, montantAlloue, montantEngage, montantRealise.add(montant));
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public int annee() { return annee; }
    public BigDecimal montantAlloue() { return montantAlloue; }
    public BigDecimal montantEngage() { return montantEngage; }
    public BigDecimal montantRealise() { return montantRealise; }
}
