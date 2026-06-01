package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class SocialDeclaration extends BaseEntity {

    private final UUID organizationId;
    private final DeclarationType type;
    private final String periode;
    private final String format;
    private final DeclarationStatus statut;
    private final UUID fichierId;
    private final Instant generatedAt;
    private final Instant submittedAt;

    private SocialDeclaration(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                              UUID organizationId, DeclarationType type, String periode,
                              String format, DeclarationStatus statut, UUID fichierId,
                              Instant generatedAt, Instant submittedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.type = Objects.requireNonNull(type);
        this.periode = Objects.requireNonNull(periode);
        this.format = Objects.requireNonNull(format);
        this.statut = Objects.requireNonNull(statut);
        this.fichierId = fichierId;
        this.generatedAt = generatedAt;
        this.submittedAt = submittedAt;
    }

    public static SocialDeclaration create(UUID tenantId, UUID organizationId, DeclarationType type,
                                           String periode, String format) {
        Instant now = Instant.now();
        return new SocialDeclaration(UUID.randomUUID(), tenantId, now, now, organizationId, type,
                periode, format, DeclarationStatus.DRAFT, null, null, null);
    }

    public static SocialDeclaration rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                              UUID organizationId, DeclarationType type, String periode,
                                              String format, DeclarationStatus statut, UUID fichierId,
                                              Instant generatedAt, Instant submittedAt) {
        return new SocialDeclaration(id, tenantId, createdAt, updatedAt, organizationId, type,
                periode, format, statut, fichierId, generatedAt, submittedAt);
    }

    public SocialDeclaration generate(UUID fichierId) {
        if (this.statut != DeclarationStatus.DRAFT) throw new IllegalStateException("Cannot generate declaration in status " + this.statut);
        Instant now = Instant.now();
        return new SocialDeclaration(id(), tenantId(), createdAt(), now, organizationId, type,
                periode, format, DeclarationStatus.GENERATED, fichierId, now, null);
    }

    public SocialDeclaration submit() {
        if (this.statut != DeclarationStatus.GENERATED) throw new IllegalStateException("Cannot submit declaration in status " + this.statut);
        Instant now = Instant.now();
        return new SocialDeclaration(id(), tenantId(), createdAt(), now, organizationId, type,
                periode, format, DeclarationStatus.SUBMITTED, fichierId, generatedAt, now);
    }

    public SocialDeclaration acknowledge() {
        if (this.statut != DeclarationStatus.SUBMITTED) throw new IllegalStateException("Cannot acknowledge declaration in status " + this.statut);
        return new SocialDeclaration(id(), tenantId(), createdAt(), Instant.now(), organizationId, type,
                periode, format, DeclarationStatus.ACKNOWLEDGED, fichierId, generatedAt, submittedAt);
    }

    public UUID organizationId() { return organizationId; }
    public DeclarationType type() { return type; }
    public String periode() { return periode; }
    public String format() { return format; }
    public DeclarationStatus statut() { return statut; }
    public UUID fichierId() { return fichierId; }
    public Instant generatedAt() { return generatedAt; }
    public Instant submittedAt() { return submittedAt; }
}
