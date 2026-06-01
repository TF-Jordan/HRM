package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class MedicalCertificate extends BaseEntity {

    private final UUID employeeId;
    private final String typeCertificat;
    private final LocalDate dateEmission;
    private final LocalDate dateExpiration;
    private final String statut;
    private final UUID fichierId;

    private MedicalCertificate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                               UUID employeeId, String typeCertificat, LocalDate dateEmission,
                               LocalDate dateExpiration, String statut, UUID fichierId) {
        super(id, tenantId, createdAt, updatedAt);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.typeCertificat = Objects.requireNonNull(typeCertificat);
        this.dateEmission = Objects.requireNonNull(dateEmission);
        this.dateExpiration = Objects.requireNonNull(dateExpiration);
        this.statut = Objects.requireNonNull(statut);
        this.fichierId = fichierId;
    }

    public static MedicalCertificate create(UUID tenantId, UUID employeeId, String typeCertificat,
                                             LocalDate dateEmission, LocalDate dateExpiration,
                                             String statut, UUID fichierId) {
        Instant now = Instant.now();
        return new MedicalCertificate(UUID.randomUUID(), tenantId, now, now, employeeId,
                typeCertificat, dateEmission, dateExpiration, statut, fichierId);
    }

    public static MedicalCertificate rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                                UUID employeeId, String typeCertificat,
                                                LocalDate dateEmission, LocalDate dateExpiration,
                                                String statut, UUID fichierId) {
        return new MedicalCertificate(id, tenantId, createdAt, updatedAt, employeeId,
                typeCertificat, dateEmission, dateExpiration, statut, fichierId);
    }

    public UUID employeeId() { return employeeId; }
    public String typeCertificat() { return typeCertificat; }
    public LocalDate dateEmission() { return dateEmission; }
    public LocalDate dateExpiration() { return dateExpiration; }
    public String statut() { return statut; }
    public UUID fichierId() { return fichierId; }
}
