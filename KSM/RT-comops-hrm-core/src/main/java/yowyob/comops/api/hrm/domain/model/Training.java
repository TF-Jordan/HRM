package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Training extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final String intitule;
    private final String organisme;
    private final LocalDate dateDebut;
    private final LocalDate dateFin;
    private final BigDecimal cout;
    private final Integer nbPlaces;
    private final String lieu;
    private final TrainingStatus status;

    private Training(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                     UUID organizationId, UUID agencyId, String intitule, String organisme,
                     LocalDate dateDebut, LocalDate dateFin, BigDecimal cout, Integer nbPlaces,
                     String lieu, TrainingStatus status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.intitule = Objects.requireNonNull(intitule);
        this.dateDebut = Objects.requireNonNull(dateDebut);
        this.status = Objects.requireNonNull(status);
        this.agencyId = agencyId;
        this.organisme = organisme;
        this.dateFin = dateFin;
        this.cout = cout;
        this.nbPlaces = nbPlaces;
        this.lieu = lieu;
    }

    public static Training create(UUID tenantId, UUID organizationId, UUID agencyId, String intitule,
                                   String organisme, LocalDate dateDebut, LocalDate dateFin,
                                   BigDecimal cout, Integer nbPlaces, String lieu) {
        Instant now = Instant.now();
        return new Training(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId,
                intitule, organisme, dateDebut, dateFin, cout, nbPlaces, lieu, TrainingStatus.PLANNED);
    }

    public static Training rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                      UUID organizationId, UUID agencyId, String intitule, String organisme,
                                      LocalDate dateDebut, LocalDate dateFin, BigDecimal cout, Integer nbPlaces,
                                      String lieu, TrainingStatus status) {
        return new Training(id, tenantId, createdAt, updatedAt, organizationId, agencyId,
                intitule, organisme, dateDebut, dateFin, cout, nbPlaces, lieu, status);
    }

    public Training start() {
        if (this.status != TrainingStatus.PLANNED) throw new IllegalStateException("Cannot start training in status " + this.status);
        return new Training(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                intitule, organisme, dateDebut, dateFin, cout, nbPlaces, lieu, TrainingStatus.IN_PROGRESS);
    }

    public Training complete() {
        if (this.status != TrainingStatus.IN_PROGRESS) throw new IllegalStateException("Cannot complete training in status " + this.status);
        return new Training(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                intitule, organisme, dateDebut, dateFin, cout, nbPlaces, lieu, TrainingStatus.COMPLETED);
    }

    public Training cancel() {
        if (this.status == TrainingStatus.COMPLETED) throw new IllegalStateException("Cannot cancel completed training");
        return new Training(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                intitule, organisme, dateDebut, dateFin, cout, nbPlaces, lieu, TrainingStatus.CANCELLED);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public String intitule() { return intitule; }
    public String organisme() { return organisme; }
    public LocalDate dateDebut() { return dateDebut; }
    public LocalDate dateFin() { return dateFin; }
    public BigDecimal cout() { return cout; }
    public Integer nbPlaces() { return nbPlaces; }
    public String lieu() { return lieu; }
    public TrainingStatus status() { return status; }
}
