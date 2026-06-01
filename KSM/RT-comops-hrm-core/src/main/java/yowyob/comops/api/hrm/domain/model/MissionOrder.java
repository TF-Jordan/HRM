package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class MissionOrder extends BaseEntity {

    private final UUID employeeId;
    private final String destination;
    private final String objet;
    private final LocalDate dateDebut;
    private final LocalDate dateFin;
    private final BigDecimal montantAvance;
    private final String centreCout;
    private final MissionOrderStatus status;
    private final UUID parentOrderId;     // non-null when this row is an avenant of a previously declined order
    private final String decisionReason;  // motif fourni par l'employé sur DECLINED
    private final Instant decidedAt;      // moment de l'acceptation OU du refus

    private MissionOrder(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                         UUID employeeId, String destination, String objet,
                         LocalDate dateDebut, LocalDate dateFin, BigDecimal montantAvance,
                         String centreCout, MissionOrderStatus status,
                         UUID parentOrderId, String decisionReason, Instant decidedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.destination = Objects.requireNonNull(destination);
        this.objet = Objects.requireNonNull(objet);
        this.dateDebut = Objects.requireNonNull(dateDebut);
        this.dateFin = Objects.requireNonNull(dateFin);
        this.status = Objects.requireNonNull(status);
        this.montantAvance = montantAvance;
        this.centreCout = centreCout;
        this.parentOrderId = parentOrderId;
        this.decisionReason = decisionReason;
        this.decidedAt = decidedAt;
    }

    public static MissionOrder create(UUID tenantId, UUID employeeId, String destination,
                                       String objet, LocalDate dateDebut, LocalDate dateFin,
                                       BigDecimal montantAvance, String centreCout) {
        return create(tenantId, employeeId, destination, objet, dateDebut, dateFin,
                montantAvance, centreCout, null);
    }

    /** Create as an amendment of {@code parentOrderId} (which must be DECLINED). */
    public static MissionOrder create(UUID tenantId, UUID employeeId, String destination,
                                       String objet, LocalDate dateDebut, LocalDate dateFin,
                                       BigDecimal montantAvance, String centreCout,
                                       UUID parentOrderId) {
        Instant now = Instant.now();
        return new MissionOrder(UUID.randomUUID(), tenantId, now, now, employeeId, destination,
                objet, dateDebut, dateFin, montantAvance, centreCout, MissionOrderStatus.DRAFT,
                parentOrderId, null, null);
    }

    public static MissionOrder rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                          UUID employeeId, String destination, String objet,
                                          LocalDate dateDebut, LocalDate dateFin, BigDecimal montantAvance,
                                          String centreCout, MissionOrderStatus status,
                                          UUID parentOrderId, String decisionReason, Instant decidedAt) {
        return new MissionOrder(id, tenantId, createdAt, updatedAt, employeeId, destination,
                objet, dateDebut, dateFin, montantAvance, centreCout, status,
                parentOrderId, decisionReason, decidedAt);
    }

    private MissionOrder withStatus(MissionOrderStatus newStatus, String reason, Instant decidedAt) {
        return new MissionOrder(id(), tenantId(), createdAt(), Instant.now(), employeeId, destination,
                objet, dateDebut, dateFin, montantAvance, centreCout, newStatus,
                parentOrderId, reason, decidedAt);
    }

    /** Manager emits the order to the employee. */
    public MissionOrder issueForAcceptance() {
        if (this.status != MissionOrderStatus.DRAFT) {
            throw new IllegalStateException("Cannot issue mission order in status " + this.status);
        }
        return withStatus(MissionOrderStatus.PENDING_ACCEPTANCE, decisionReason, decidedAt);
    }

    /** Employee accepts → APPROVED, ready to start. */
    public MissionOrder acceptByEmployee() {
        if (this.status != MissionOrderStatus.PENDING_ACCEPTANCE) {
            throw new IllegalStateException("Cannot accept mission order in status " + this.status);
        }
        return withStatus(MissionOrderStatus.APPROVED, null, Instant.now());
    }

    /** Employee declines → DECLINED with a mandatory textual reason. */
    public MissionOrder declineByEmployee(String reason) {
        if (this.status != MissionOrderStatus.PENDING_ACCEPTANCE) {
            throw new IllegalStateException("Cannot decline mission order in status " + this.status);
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Decline reason is required");
        }
        return withStatus(MissionOrderStatus.DECLINED, reason.trim(), Instant.now());
    }

    public MissionOrder start() {
        if (this.status != MissionOrderStatus.APPROVED) {
            throw new IllegalStateException("Cannot start mission order in status " + this.status);
        }
        return withStatus(MissionOrderStatus.IN_PROGRESS, decisionReason, decidedAt);
    }

    public MissionOrder complete() {
        if (this.status != MissionOrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete mission order in status " + this.status);
        }
        return withStatus(MissionOrderStatus.COMPLETED, decisionReason, decidedAt);
    }

    public MissionOrder cancel() {
        if (this.status == MissionOrderStatus.COMPLETED || this.status == MissionOrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel mission order in status " + this.status);
        }
        return withStatus(MissionOrderStatus.CANCELLED, decisionReason, decidedAt);
    }

    public UUID employeeId() { return employeeId; }
    public String destination() { return destination; }
    public String objet() { return objet; }
    public LocalDate dateDebut() { return dateDebut; }
    public LocalDate dateFin() { return dateFin; }
    public BigDecimal montantAvance() { return montantAvance; }
    public String centreCout() { return centreCout; }
    public MissionOrderStatus status() { return status; }
    public UUID parentOrderId() { return parentOrderId; }
    public String decisionReason() { return decisionReason; }
    public Instant decidedAt() { return decidedAt; }
}
