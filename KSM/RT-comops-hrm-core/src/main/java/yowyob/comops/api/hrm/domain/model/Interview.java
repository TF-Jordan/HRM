package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Interview extends BaseEntity {

    private final UUID applicationId;
    private final InterviewType type;
    private final Instant dateHeure;
    private final String lieu;
    private final UUID interviewerPartyId;
    private final String interviewerDisplayName;
    private final String notes;
    private final InterviewResult resultat;

    private Interview(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                      UUID applicationId, InterviewType type, Instant dateHeure, String lieu,
                      UUID interviewerPartyId, String interviewerDisplayName,
                      String notes, InterviewResult resultat) {
        super(id, tenantId, createdAt, updatedAt);
        this.applicationId = Objects.requireNonNull(applicationId);
        this.type = Objects.requireNonNull(type);
        this.dateHeure = Objects.requireNonNull(dateHeure);
        this.resultat = Objects.requireNonNull(resultat);
        this.lieu = lieu;
        this.interviewerPartyId = interviewerPartyId;
        this.interviewerDisplayName = interviewerDisplayName;
        this.notes = notes;
    }

    public static Interview create(UUID tenantId, UUID applicationId, InterviewType type,
                                    Instant dateHeure, String lieu, UUID interviewerPartyId,
                                    String interviewerDisplayName) {
        Instant now = Instant.now();
        return new Interview(UUID.randomUUID(), tenantId, now, now, applicationId, type,
                dateHeure, lieu, interviewerPartyId, interviewerDisplayName, null, InterviewResult.PENDING);
    }

    public static Interview rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                       UUID applicationId, InterviewType type, Instant dateHeure, String lieu,
                                       UUID interviewerPartyId, String interviewerDisplayName,
                                       String notes, InterviewResult resultat) {
        return new Interview(id, tenantId, createdAt, updatedAt, applicationId, type,
                dateHeure, lieu, interviewerPartyId, interviewerDisplayName, notes, resultat);
    }

    public Interview complete(String notes, InterviewResult resultat) {
        if (this.resultat != InterviewResult.PENDING) throw new IllegalStateException("Interview already completed");
        return new Interview(id(), tenantId(), createdAt(), Instant.now(), applicationId, type,
                dateHeure, lieu, interviewerPartyId, interviewerDisplayName, notes, resultat);
    }

    public UUID applicationId() { return applicationId; }
    public InterviewType type() { return type; }
    public Instant dateHeure() { return dateHeure; }
    public String lieu() { return lieu; }
    public UUID interviewerPartyId() { return interviewerPartyId; }
    public String interviewerDisplayName() { return interviewerDisplayName; }
    public String notes() { return notes; }
    public InterviewResult resultat() { return resultat; }
}
