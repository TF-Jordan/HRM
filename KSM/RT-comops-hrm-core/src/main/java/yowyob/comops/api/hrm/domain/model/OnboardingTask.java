package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class OnboardingTask extends BaseEntity {

    private final UUID employeeId;
    private final String titre;
    private final String description;
    private final UUID assignedToPartyId;
    private final LocalDate echeance;
    private final OnboardingTaskStatus status;

    private OnboardingTask(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                           UUID employeeId, String titre, String description,
                           UUID assignedToPartyId, LocalDate echeance, OnboardingTaskStatus status) {
        super(id, tenantId, createdAt, updatedAt);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.titre = Objects.requireNonNull(titre);
        this.status = Objects.requireNonNull(status);
        this.description = description;
        this.assignedToPartyId = assignedToPartyId;
        this.echeance = echeance;
    }

    public static OnboardingTask create(UUID tenantId, UUID employeeId, String titre,
                                         String description, UUID assignedToPartyId, LocalDate echeance) {
        Instant now = Instant.now();
        return new OnboardingTask(UUID.randomUUID(), tenantId, now, now, employeeId, titre,
                description, assignedToPartyId, echeance, OnboardingTaskStatus.PENDING);
    }

    public static OnboardingTask rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                            UUID employeeId, String titre, String description,
                                            UUID assignedToPartyId, LocalDate echeance, OnboardingTaskStatus status) {
        return new OnboardingTask(id, tenantId, createdAt, updatedAt, employeeId, titre,
                description, assignedToPartyId, echeance, status);
    }

    public OnboardingTask start() {
        if (this.status != OnboardingTaskStatus.PENDING) throw new IllegalStateException("Cannot start task in status " + this.status);
        return new OnboardingTask(id(), tenantId(), createdAt(), Instant.now(), employeeId, titre,
                description, assignedToPartyId, echeance, OnboardingTaskStatus.IN_PROGRESS);
    }

    public OnboardingTask complete() {
        if (this.status == OnboardingTaskStatus.COMPLETED) throw new IllegalStateException("Task already completed");
        return new OnboardingTask(id(), tenantId(), createdAt(), Instant.now(), employeeId, titre,
                description, assignedToPartyId, echeance, OnboardingTaskStatus.COMPLETED);
    }

    public UUID employeeId() { return employeeId; }
    public String titre() { return titre; }
    public String description() { return description; }
    public UUID assignedToPartyId() { return assignedToPartyId; }
    public LocalDate echeance() { return echeance; }
    public OnboardingTaskStatus status() { return status; }
}
