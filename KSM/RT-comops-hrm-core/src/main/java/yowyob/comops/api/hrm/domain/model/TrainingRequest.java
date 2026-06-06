package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * An employee's self-service request to join a training session. The request goes
 * through a PENDING → APPROVED/REJECTED workflow; on approval the manager/DRH turns
 * it into a {@link TrainingEnrollment}, whose id is linked back here. The employee
 * may cancel their own request while it is still PENDING.
 */
public final class TrainingRequest extends BaseEntity {

    private final UUID organizationId;   // denormalized from the training, drives the approval queue
    private final UUID employeeId;       // the requester
    private final UUID trainingId;       // the session they want to join
    private final String motivation;     // why the employee wants this training
    private final TrainingRequestStatus status;
    private final String decisionReason; // mandatory reason on REJECTED
    private final UUID enrollmentId;      // set when APPROVED → the created enrollment
    private final Instant decidedAt;      // moment of approval / rejection / cancellation

    private TrainingRequest(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                            UUID organizationId, UUID employeeId, UUID trainingId, String motivation,
                            TrainingRequestStatus status, String decisionReason,
                            UUID enrollmentId, Instant decidedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.trainingId = Objects.requireNonNull(trainingId);
        this.status = Objects.requireNonNull(status);
        this.motivation = motivation;
        this.decisionReason = decisionReason;
        this.enrollmentId = enrollmentId;
        this.decidedAt = decidedAt;
    }

    public static TrainingRequest create(UUID tenantId, UUID organizationId, UUID employeeId,
                                         UUID trainingId, String motivation) {
        Instant now = Instant.now();
        return new TrainingRequest(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId,
                trainingId, motivation, TrainingRequestStatus.PENDING, null, null, null);
    }

    public static TrainingRequest rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                            UUID organizationId, UUID employeeId, UUID trainingId,
                                            String motivation, TrainingRequestStatus status,
                                            String decisionReason, UUID enrollmentId, Instant decidedAt) {
        return new TrainingRequest(id, tenantId, createdAt, updatedAt, organizationId, employeeId,
                trainingId, motivation, status, decisionReason, enrollmentId, decidedAt);
    }

    /** Manager/DRH approves → links the created enrollment. */
    public TrainingRequest approve(UUID enrollmentId) {
        if (this.status != TrainingRequestStatus.PENDING) {
            throw new IllegalStateException("Cannot approve training request in status " + this.status);
        }
        Objects.requireNonNull(enrollmentId, "enrollmentId is required on approval");
        return new TrainingRequest(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                trainingId, motivation, TrainingRequestStatus.APPROVED, null, enrollmentId, Instant.now());
    }

    /** Manager/DRH rejects with a mandatory reason. */
    public TrainingRequest reject(String reason) {
        if (this.status != TrainingRequestStatus.PENDING) {
            throw new IllegalStateException("Cannot reject training request in status " + this.status);
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reject reason is required");
        }
        return new TrainingRequest(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                trainingId, motivation, TrainingRequestStatus.REJECTED, reason.trim(), null, Instant.now());
    }

    /** Employee withdraws their own still-pending request. */
    public TrainingRequest cancel() {
        if (this.status != TrainingRequestStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel training request in status " + this.status);
        }
        return new TrainingRequest(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                trainingId, motivation, TrainingRequestStatus.CANCELLED, decisionReason, enrollmentId, Instant.now());
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public UUID trainingId() { return trainingId; }
    public String motivation() { return motivation; }
    public TrainingRequestStatus status() { return status; }
    public String decisionReason() { return decisionReason; }
    public UUID enrollmentId() { return enrollmentId; }
    public Instant decidedAt() { return decidedAt; }
}
