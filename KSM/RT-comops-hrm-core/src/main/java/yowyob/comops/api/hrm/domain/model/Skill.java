package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Skill extends BaseEntity {

    private final String name;
    private final String categorie;
    private final String description;

    private Skill(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                  String name, String categorie, String description) {
        super(id, tenantId, createdAt, updatedAt);
        this.name = Objects.requireNonNull(name);
        this.categorie = categorie;
        this.description = description;
    }

    public static Skill create(UUID tenantId, String name, String categorie, String description) {
        Instant now = Instant.now();
        return new Skill(UUID.randomUUID(), tenantId, now, now, name, categorie, description);
    }

    public static Skill rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                   String name, String categorie, String description) {
        return new Skill(id, tenantId, createdAt, updatedAt, name, categorie, description);
    }

    public String name() { return name; }
    public String categorie() { return categorie; }
    public String description() { return description; }
}
