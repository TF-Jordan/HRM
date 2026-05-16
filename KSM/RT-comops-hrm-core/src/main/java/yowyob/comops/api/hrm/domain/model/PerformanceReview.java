package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PerformanceReview extends BaseEntity {

    private final UUID organizationId;
    private final UUID employeeId;
    private final UUID evaluateurPartyId;
    private final String evaluateurDisplayName;
    private final String periode;
    private final BigDecimal noteGlobale;
    private final String commentaires;
    private final String planAction;
    private final ReviewStatus status;

    private PerformanceReview(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                              UUID organizationId, UUID employeeId, UUID evaluateurPartyId,
                              String evaluateurDisplayName, String periode, BigDecimal noteGlobale,
                              String commentaires, String planAction, ReviewStatus status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.periode = Objects.requireNonNull(periode);
        this.status = Objects.requireNonNull(status);
        this.evaluateurPartyId = evaluateurPartyId;
        this.evaluateurDisplayName = evaluateurDisplayName;
        this.noteGlobale = noteGlobale;
        this.commentaires = commentaires;
        this.planAction = planAction;
    }

    public static PerformanceReview create(UUID tenantId, UUID organizationId, UUID employeeId,
                                            UUID evaluateurPartyId, String evaluateurDisplayName,
                                            String periode) {
        Instant now = Instant.now();
        return new PerformanceReview(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId,
                evaluateurPartyId, evaluateurDisplayName, periode, null, null, null, ReviewStatus.DRAFT);
    }

    public static PerformanceReview rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                               UUID organizationId, UUID employeeId, UUID evaluateurPartyId,
                                               String evaluateurDisplayName, String periode, BigDecimal noteGlobale,
                                               String commentaires, String planAction, ReviewStatus status) {
        return new PerformanceReview(id, tenantId, createdAt, updatedAt, organizationId, employeeId,
                evaluateurPartyId, evaluateurDisplayName, periode, noteGlobale, commentaires, planAction, status);
    }

    public PerformanceReview submit(BigDecimal note, String commentaires, String planAction) {
        if (this.status != ReviewStatus.DRAFT) throw new IllegalStateException("Cannot submit review in status " + this.status);
        return new PerformanceReview(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                evaluateurPartyId, evaluateurDisplayName, periode, note, commentaires, planAction, ReviewStatus.SUBMITTED);
    }

    public PerformanceReview acknowledge() {
        if (this.status != ReviewStatus.SUBMITTED) throw new IllegalStateException("Cannot acknowledge review in status " + this.status);
        return new PerformanceReview(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                evaluateurPartyId, evaluateurDisplayName, periode, noteGlobale, commentaires, planAction, ReviewStatus.ACKNOWLEDGED);
    }

    public PerformanceReview finalize_() {
        if (this.status != ReviewStatus.ACKNOWLEDGED) throw new IllegalStateException("Cannot finalize review in status " + this.status);
        return new PerformanceReview(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                evaluateurPartyId, evaluateurDisplayName, periode, noteGlobale, commentaires, planAction, ReviewStatus.FINALIZED);
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public UUID evaluateurPartyId() { return evaluateurPartyId; }
    public String evaluateurDisplayName() { return evaluateurDisplayName; }
    public String periode() { return periode; }
    public BigDecimal noteGlobale() { return noteGlobale; }
    public String commentaires() { return commentaires; }
    public String planAction() { return planAction; }
    public ReviewStatus status() { return status; }
}
