package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

public final class Contract extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID employeeId;
    private final ContractType type;
    private final LocalDate dateDebut;
    private final LocalDate dateFin;
    private final BigDecimal salaireBase;
    private final BigDecimal avantagesNature;
    private final Integer periodeEssai;
    private final ContractStatus status;
    private final String motifFin;
    private final UUID documentFileId;

    private Contract(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                     UUID organizationId, UUID agencyId, UUID employeeId, ContractType type,
                     LocalDate dateDebut, LocalDate dateFin, BigDecimal salaireBase,
                     BigDecimal avantagesNature, Integer periodeEssai, ContractStatus status,
                     String motifFin, UUID documentFileId) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.agencyId = agencyId;
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.dateDebut = Objects.requireNonNull(dateDebut, "dateDebut is required");
        this.dateFin = dateFin;
        this.salaireBase = Objects.requireNonNull(salaireBase, "salaireBase is required");
        this.avantagesNature = avantagesNature != null ? avantagesNature : BigDecimal.ZERO;
        this.periodeEssai = periodeEssai;
        this.status = Objects.requireNonNull(status, "status is required");
        this.motifFin = motifFin;
        this.documentFileId = documentFileId;
    }

    public static Contract create(UUID tenantId, UUID organizationId, UUID agencyId, UUID employeeId,
                                  ContractType type, LocalDate dateDebut, LocalDate dateFin,
                                  BigDecimal salaireBase, BigDecimal avantagesNature, Integer periodeEssai) {
        return create(tenantId, organizationId, agencyId, employeeId, type, dateDebut, dateFin,
                salaireBase, avantagesNature, periodeEssai, null);
    }

    public static Contract create(UUID tenantId, UUID organizationId, UUID agencyId, UUID employeeId,
                                  ContractType type, LocalDate dateDebut, LocalDate dateFin,
                                  BigDecimal salaireBase, BigDecimal avantagesNature, Integer periodeEssai,
                                  UUID documentFileId) {
        Instant now = Instant.now();
        return new Contract(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId,
                employeeId, type, dateDebut, dateFin, salaireBase, avantagesNature, periodeEssai,
                ContractStatus.ACTIVE, null, documentFileId);
    }

    public static Contract rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                     UUID organizationId, UUID agencyId, UUID employeeId, ContractType type,
                                     LocalDate dateDebut, LocalDate dateFin, BigDecimal salaireBase,
                                     BigDecimal avantagesNature, Integer periodeEssai, ContractStatus status,
                                     String motifFin, UUID documentFileId) {
        return new Contract(id, tenantId, createdAt, updatedAt, organizationId, agencyId, employeeId,
                type, dateDebut, dateFin, salaireBase, avantagesNature, periodeEssai, status,
                motifFin, documentFileId);
    }

    public boolean isExpiringSoon(int days) {
        if (dateFin == null) return false;
        return LocalDate.now().until(dateFin, ChronoUnit.DAYS) <= days;
    }

    public Contract terminate(String motif) {
        return new Contract(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                employeeId, type, dateDebut, dateFin, salaireBase, avantagesNature, periodeEssai,
                ContractStatus.TERMINATED, motif, documentFileId);
    }

    public Contract renew(LocalDate newDateFin) {
        return new Contract(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                employeeId, type, dateDebut, newDateFin, salaireBase, avantagesNature, periodeEssai,
                ContractStatus.RENEWED, null, documentFileId);
    }

    public Contract attachDocument(UUID fileId) {
        return new Contract(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                employeeId, type, dateDebut, dateFin, salaireBase, avantagesNature, periodeEssai,
                status, motifFin, fileId);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID employeeId() { return employeeId; }
    public ContractType type() { return type; }
    public LocalDate dateDebut() { return dateDebut; }
    public LocalDate dateFin() { return dateFin; }
    public BigDecimal salaireBase() { return salaireBase; }
    public BigDecimal avantagesNature() { return avantagesNature; }
    public Integer periodeEssai() { return periodeEssai; }
    public ContractStatus status() { return status; }
    public String motifFin() { return motifFin; }
    public UUID documentFileId() { return documentFileId; }
}
