package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Dependent extends BaseEntity {

    private final UUID organizationId;
    private final UUID employeeId;
    private final String nom;
    private final String prenom;
    private final LocalDate dateNaissance;
    private final String lienParente;
    private final UUID certificatFileId;

    private Dependent(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                      UUID organizationId, UUID employeeId, String nom, String prenom,
                      LocalDate dateNaissance, String lienParente, UUID certificatFileId) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.nom = Objects.requireNonNull(nom, "nom is required");
        this.prenom = Objects.requireNonNull(prenom, "prenom is required");
        this.dateNaissance = Objects.requireNonNull(dateNaissance, "dateNaissance is required");
        this.lienParente = Objects.requireNonNull(lienParente, "lienParente is required");
        this.certificatFileId = certificatFileId;
    }

    public static Dependent create(UUID tenantId, UUID organizationId, UUID employeeId,
                                   String nom, String prenom, LocalDate dateNaissance,
                                   String lienParente) {
        Instant now = Instant.now();
        return new Dependent(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId,
                nom, prenom, dateNaissance, lienParente, null);
    }

    public static Dependent rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                      UUID organizationId, UUID employeeId, String nom, String prenom,
                                      LocalDate dateNaissance, String lienParente, UUID certificatFileId) {
        return new Dependent(id, tenantId, createdAt, updatedAt, organizationId, employeeId,
                nom, prenom, dateNaissance, lienParente, certificatFileId);
    }

    public Dependent attachCertificat(UUID fileId) {
        return new Dependent(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                employeeId, nom, prenom, dateNaissance, lienParente, fileId);
    }

    public boolean isUnderAge(int ageLimit) {
        return LocalDate.now().minusYears(ageLimit).isBefore(dateNaissance);
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public String nom() { return nom; }
    public String prenom() { return prenom; }
    public LocalDate dateNaissance() { return dateNaissance; }
    public String lienParente() { return lienParente; }
    public UUID certificatFileId() { return certificatFileId; }
}
