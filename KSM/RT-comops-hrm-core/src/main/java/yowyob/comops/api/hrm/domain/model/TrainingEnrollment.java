package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class TrainingEnrollment extends BaseEntity {

    private final UUID trainingId;
    private final UUID employeeId;
    private final TrainingEnrollmentStatus status;
    private final BigDecimal noteEvaluation;
    private final UUID attestationFileId;

    private TrainingEnrollment(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                               UUID trainingId, UUID employeeId, TrainingEnrollmentStatus status,
                               BigDecimal noteEvaluation, UUID attestationFileId) {
        super(id, tenantId, createdAt, updatedAt);
        this.trainingId = Objects.requireNonNull(trainingId);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.status = Objects.requireNonNull(status);
        this.noteEvaluation = noteEvaluation;
        this.attestationFileId = attestationFileId;
    }

    public static TrainingEnrollment enroll(UUID tenantId, UUID trainingId, UUID employeeId) {
        Instant now = Instant.now();
        return new TrainingEnrollment(UUID.randomUUID(), tenantId, now, now, trainingId, employeeId,
                TrainingEnrollmentStatus.ENROLLED, null, null);
    }

    public static TrainingEnrollment rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                                UUID trainingId, UUID employeeId, TrainingEnrollmentStatus status,
                                                BigDecimal noteEvaluation, UUID attestationFileId) {
        return new TrainingEnrollment(id, tenantId, createdAt, updatedAt, trainingId, employeeId,
                status, noteEvaluation, attestationFileId);
    }

    public TrainingEnrollment complete(BigDecimal note, UUID attestationId) {
        if (this.status != TrainingEnrollmentStatus.ENROLLED) throw new IllegalStateException("Cannot complete enrollment in status " + this.status);
        return new TrainingEnrollment(id(), tenantId(), createdAt(), Instant.now(), trainingId, employeeId,
                TrainingEnrollmentStatus.COMPLETED, note, attestationId);
    }

    public TrainingEnrollment cancel() {
        if (this.status != TrainingEnrollmentStatus.ENROLLED) throw new IllegalStateException("Cannot cancel enrollment in status " + this.status);
        return new TrainingEnrollment(id(), tenantId(), createdAt(), Instant.now(), trainingId, employeeId,
                TrainingEnrollmentStatus.CANCELLED, noteEvaluation, attestationFileId);
    }

    public UUID trainingId() { return trainingId; }
    public UUID employeeId() { return employeeId; }
    public TrainingEnrollmentStatus status() { return status; }
    public BigDecimal noteEvaluation() { return noteEvaluation; }
    public UUID attestationFileId() { return attestationFileId; }
}
