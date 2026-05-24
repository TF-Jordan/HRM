package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class LeaveRequest extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID employeeId;
    private final LeaveType type;
    private final LocalDate dateDebut;
    private final LocalDate dateFin;
    private final BigDecimal nbJours;
    private final LeaveStatus status;
    private final String motif;
    private final UUID valideurPartyId;
    private final String valideurDisplayName;
    private final Instant dateValidation;
    private final String commentaireValideur;
    private final UUID justificatifFileId;

    private LeaveRequest(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                         UUID organizationId, UUID agencyId, UUID employeeId, LeaveType type,
                         LocalDate dateDebut, LocalDate dateFin, BigDecimal nbJours, LeaveStatus status,
                         String motif, UUID valideurPartyId, String valideurDisplayName,
                         Instant dateValidation, String commentaireValideur, UUID justificatifFileId) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.dateDebut = Objects.requireNonNull(dateDebut, "dateDebut is required");
        this.dateFin = Objects.requireNonNull(dateFin, "dateFin is required");
        this.nbJours = Objects.requireNonNull(nbJours, "nbJours is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.agencyId = agencyId;
        this.motif = motif;
        this.valideurPartyId = valideurPartyId;
        this.valideurDisplayName = valideurDisplayName;
        this.dateValidation = dateValidation;
        this.commentaireValideur = commentaireValideur;
        this.justificatifFileId = justificatifFileId;
    }

    public static LeaveRequest submit(UUID tenantId, UUID organizationId, UUID agencyId,
                                       UUID employeeId, LeaveType type, LocalDate dateDebut,
                                       LocalDate dateFin, BigDecimal nbJours, String motif,
                                       UUID valideurPartyId, String valideurDisplayName,
                                       UUID justificatifFileId) {
        if (dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("Leave end date must be on or after start date");
        }
        if (dateDebut.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Leave start date must not be in the past");
        }
        Instant now = Instant.now();
        return new LeaveRequest(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId,
                employeeId, type, dateDebut, dateFin, nbJours, LeaveStatus.PENDING, motif,
                valideurPartyId, valideurDisplayName, null, null, justificatifFileId);
    }

    public static LeaveRequest rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                          UUID organizationId, UUID agencyId, UUID employeeId,
                                          LeaveType type, LocalDate dateDebut, LocalDate dateFin,
                                          BigDecimal nbJours, LeaveStatus status, String motif,
                                          UUID valideurPartyId, String valideurDisplayName,
                                          Instant dateValidation, String commentaireValideur,
                                          UUID justificatifFileId) {
        return new LeaveRequest(id, tenantId, createdAt, updatedAt, organizationId, agencyId,
                employeeId, type, dateDebut, dateFin, nbJours, status, motif,
                valideurPartyId, valideurDisplayName, dateValidation, commentaireValideur,
                justificatifFileId);
    }

    public LeaveRequest approve(UUID managerId) {
        if (this.status != LeaveStatus.PENDING) {
            throw new IllegalStateException("Cannot approve leave request in status " + this.status);
        }
        return new LeaveRequest(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                employeeId, type, dateDebut, dateFin, nbJours, LeaveStatus.APPROVED, motif,
                valideurPartyId, valideurDisplayName, Instant.now(), commentaireValideur,
                justificatifFileId);
    }

    public LeaveRequest reject(UUID managerId, String commentaire) {
        if (this.status != LeaveStatus.PENDING) {
            throw new IllegalStateException("Cannot reject leave request in status " + this.status);
        }
        return new LeaveRequest(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                employeeId, type, dateDebut, dateFin, nbJours, LeaveStatus.REJECTED, motif,
                valideurPartyId, valideurDisplayName, Instant.now(), commentaire,
                justificatifFileId);
    }

    public LeaveRequest cancel() {
        if (this.status != LeaveStatus.PENDING && this.status != LeaveStatus.APPROVED) {
            throw new IllegalStateException("Cannot cancel leave request in status " + this.status);
        }
        if (this.status == LeaveStatus.APPROVED && !LocalDate.now().isBefore(this.dateDebut)) {
            throw new IllegalStateException("Cannot cancel approved leave after start date");
        }
        return new LeaveRequest(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                employeeId, type, dateDebut, dateFin, nbJours, LeaveStatus.CANCELLED, motif,
                valideurPartyId, valideurDisplayName, dateValidation, commentaireValideur,
                justificatifFileId);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID employeeId() { return employeeId; }
    public LeaveType type() { return type; }
    public LocalDate dateDebut() { return dateDebut; }
    public LocalDate dateFin() { return dateFin; }
    public BigDecimal nbJours() { return nbJours; }
    public LeaveStatus status() { return status; }
    public String motif() { return motif; }
    public UUID valideurPartyId() { return valideurPartyId; }
    public String valideurDisplayName() { return valideurDisplayName; }
    public Instant dateValidation() { return dateValidation; }
    public String commentaireValideur() { return commentaireValideur; }
    public UUID justificatifFileId() { return justificatifFileId; }
}
