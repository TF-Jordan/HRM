package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class MedicalVisit extends BaseEntity {

    private final UUID employeeId;
    private final LocalDate dateVisite;
    private final String medecin;
    private final AptitudeResult resultatAptitude;
    private final String restrictions;
    private final LocalDate prochaineEcheance;
    private final UUID certificatFileId;

    private MedicalVisit(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                         UUID employeeId, LocalDate dateVisite, String medecin,
                         AptitudeResult resultatAptitude, String restrictions,
                         LocalDate prochaineEcheance, UUID certificatFileId) {
        super(id, tenantId, createdAt, updatedAt);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.dateVisite = Objects.requireNonNull(dateVisite);
        this.medecin = Objects.requireNonNull(medecin);
        this.resultatAptitude = Objects.requireNonNull(resultatAptitude);
        this.restrictions = restrictions;
        this.prochaineEcheance = Objects.requireNonNull(prochaineEcheance);
        this.certificatFileId = certificatFileId;
    }

    public static MedicalVisit create(UUID tenantId, UUID employeeId, LocalDate dateVisite,
                                       String medecin, AptitudeResult resultatAptitude,
                                       String restrictions, LocalDate prochaineEcheance,
                                       UUID certificatFileId) {
        Instant now = Instant.now();
        return new MedicalVisit(UUID.randomUUID(), tenantId, now, now, employeeId, dateVisite,
                medecin, resultatAptitude, restrictions, prochaineEcheance, certificatFileId);
    }

    public static MedicalVisit rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                          UUID employeeId, LocalDate dateVisite, String medecin,
                                          AptitudeResult resultatAptitude, String restrictions,
                                          LocalDate prochaineEcheance, UUID certificatFileId) {
        return new MedicalVisit(id, tenantId, createdAt, updatedAt, employeeId, dateVisite,
                medecin, resultatAptitude, restrictions, prochaineEcheance, certificatFileId);
    }

    public UUID employeeId() { return employeeId; }
    public LocalDate dateVisite() { return dateVisite; }
    public String medecin() { return medecin; }
    public AptitudeResult resultatAptitude() { return resultatAptitude; }
    public String restrictions() { return restrictions; }
    public LocalDate prochaineEcheance() { return prochaineEcheance; }
    public UUID certificatFileId() { return certificatFileId; }
}
