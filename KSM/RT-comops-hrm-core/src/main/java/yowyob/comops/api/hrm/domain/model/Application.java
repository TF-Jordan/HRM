package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Application extends BaseEntity {

    private final UUID jobOfferId;
    private final String candidatNom;
    private final String candidatPrenom;
    private final String candidatEmail;
    private final String candidatTelephone;
    private final UUID cvFileId;
    private final UUID lettreMotivationFileId;
    private final ApplicationStatus status;

    private Application(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                        UUID jobOfferId, String candidatNom, String candidatPrenom,
                        String candidatEmail, String candidatTelephone, UUID cvFileId,
                        UUID lettreMotivationFileId, ApplicationStatus status) {
        super(id, tenantId, createdAt, updatedAt);
        this.jobOfferId = Objects.requireNonNull(jobOfferId);
        this.candidatNom = Objects.requireNonNull(candidatNom);
        this.candidatPrenom = Objects.requireNonNull(candidatPrenom);
        this.status = Objects.requireNonNull(status);
        this.candidatEmail = candidatEmail;
        this.candidatTelephone = candidatTelephone;
        this.cvFileId = cvFileId;
        this.lettreMotivationFileId = lettreMotivationFileId;
    }

    public static Application create(UUID tenantId, UUID jobOfferId, String candidatNom,
                                      String candidatPrenom, String candidatEmail,
                                      String candidatTelephone, UUID cvFileId,
                                      UUID lettreMotivationFileId) {
        Instant now = Instant.now();
        return new Application(UUID.randomUUID(), tenantId, now, now, jobOfferId, candidatNom,
                candidatPrenom, candidatEmail, candidatTelephone, cvFileId, lettreMotivationFileId,
                ApplicationStatus.NEW);
    }

    public static Application rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                         UUID jobOfferId, String candidatNom, String candidatPrenom,
                                         String candidatEmail, String candidatTelephone, UUID cvFileId,
                                         UUID lettreMotivationFileId, ApplicationStatus status) {
        return new Application(id, tenantId, createdAt, updatedAt, jobOfferId, candidatNom,
                candidatPrenom, candidatEmail, candidatTelephone, cvFileId, lettreMotivationFileId, status);
    }

    public Application shortlist() {
        if (this.status != ApplicationStatus.NEW) throw new IllegalStateException("Cannot shortlist in status " + this.status);
        return new Application(id(), tenantId(), createdAt(), Instant.now(), jobOfferId, candidatNom,
                candidatPrenom, candidatEmail, candidatTelephone, cvFileId, lettreMotivationFileId, ApplicationStatus.SHORTLISTED);
    }

    public Application interview() {
        if (this.status != ApplicationStatus.SHORTLISTED) throw new IllegalStateException("Cannot interview in status " + this.status);
        return new Application(id(), tenantId(), createdAt(), Instant.now(), jobOfferId, candidatNom,
                candidatPrenom, candidatEmail, candidatTelephone, cvFileId, lettreMotivationFileId, ApplicationStatus.INTERVIEWING);
    }

    public Application offer() {
        if (this.status != ApplicationStatus.INTERVIEWING) throw new IllegalStateException("Cannot offer in status " + this.status);
        return new Application(id(), tenantId(), createdAt(), Instant.now(), jobOfferId, candidatNom,
                candidatPrenom, candidatEmail, candidatTelephone, cvFileId, lettreMotivationFileId, ApplicationStatus.OFFERED);
    }

    public Application reject() {
        if (this.status == ApplicationStatus.HIRED || this.status == ApplicationStatus.REJECTED)
            throw new IllegalStateException("Cannot reject in status " + this.status);
        return new Application(id(), tenantId(), createdAt(), Instant.now(), jobOfferId, candidatNom,
                candidatPrenom, candidatEmail, candidatTelephone, cvFileId, lettreMotivationFileId, ApplicationStatus.REJECTED);
    }

    public Application hire() {
        if (this.status != ApplicationStatus.OFFERED) throw new IllegalStateException("Cannot hire in status " + this.status);
        return new Application(id(), tenantId(), createdAt(), Instant.now(), jobOfferId, candidatNom,
                candidatPrenom, candidatEmail, candidatTelephone, cvFileId, lettreMotivationFileId, ApplicationStatus.HIRED);
    }

    public UUID jobOfferId() { return jobOfferId; }
    public String candidatNom() { return candidatNom; }
    public String candidatPrenom() { return candidatPrenom; }
    public String candidatEmail() { return candidatEmail; }
    public String candidatTelephone() { return candidatTelephone; }
    public UUID cvFileId() { return cvFileId; }
    public UUID lettreMotivationFileId() { return lettreMotivationFileId; }
    public ApplicationStatus status() { return status; }
}
