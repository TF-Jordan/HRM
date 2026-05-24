package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class JobOffer extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final String poste;
    private final String departement;
    private final String localisation;
    private final String competencesRequises;
    private final LocalDate dateLimite;
    private final String packageSalarial;
    private final JobOfferStatus status;

    private JobOffer(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                     UUID organizationId, UUID agencyId, String poste, String departement,
                     String localisation, String competencesRequises, LocalDate dateLimite,
                     String packageSalarial, JobOfferStatus status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.poste = Objects.requireNonNull(poste);
        this.status = Objects.requireNonNull(status);
        this.agencyId = agencyId;
        this.departement = departement;
        this.localisation = localisation;
        this.competencesRequises = competencesRequises;
        this.dateLimite = dateLimite;
        this.packageSalarial = packageSalarial;
    }

    public static JobOffer create(UUID tenantId, UUID organizationId, UUID agencyId, String poste,
                                   String departement, String localisation, String competencesRequises,
                                   LocalDate dateLimite, String packageSalarial) {
        if (dateLimite != null && dateLimite.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Application deadline must not be in the past");
        }
        Instant now = Instant.now();
        return new JobOffer(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId,
                poste, departement, localisation, competencesRequises, dateLimite, packageSalarial,
                JobOfferStatus.DRAFT);
    }

    public static JobOffer rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                      UUID organizationId, UUID agencyId, String poste, String departement,
                                      String localisation, String competencesRequises, LocalDate dateLimite,
                                      String packageSalarial, JobOfferStatus status) {
        return new JobOffer(id, tenantId, createdAt, updatedAt, organizationId, agencyId,
                poste, departement, localisation, competencesRequises, dateLimite, packageSalarial, status);
    }

    public JobOffer publish() {
        if (this.status != JobOfferStatus.DRAFT) throw new IllegalStateException("Cannot publish offer in status " + this.status);
        return new JobOffer(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                poste, departement, localisation, competencesRequises, dateLimite, packageSalarial, JobOfferStatus.PUBLISHED);
    }

    public JobOffer close() {
        if (this.status != JobOfferStatus.PUBLISHED) throw new IllegalStateException("Cannot close offer in status " + this.status);
        return new JobOffer(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                poste, departement, localisation, competencesRequises, dateLimite, packageSalarial, JobOfferStatus.CLOSED);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public String poste() { return poste; }
    public String departement() { return departement; }
    public String localisation() { return localisation; }
    public String competencesRequises() { return competencesRequises; }
    public LocalDate dateLimite() { return dateLimite; }
    public String packageSalarial() { return packageSalarial; }
    public JobOfferStatus status() { return status; }
}
