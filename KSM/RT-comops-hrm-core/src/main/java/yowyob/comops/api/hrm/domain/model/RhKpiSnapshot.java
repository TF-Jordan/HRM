package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class RhKpiSnapshot extends BaseEntity {

    private final UUID organizationId;
    private final String periode;
    private final int effectifTotal;
    private final int effectifActif;
    private final BigDecimal tauxTurnover;
    private final BigDecimal tauxAbsenteisme;
    private final BigDecimal masseSalariale;
    private final BigDecimal couvertureCompetences;

    private RhKpiSnapshot(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                          UUID organizationId, String periode, int effectifTotal, int effectifActif,
                          BigDecimal tauxTurnover, BigDecimal tauxAbsenteisme,
                          BigDecimal masseSalariale, BigDecimal couvertureCompetences) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.periode = Objects.requireNonNull(periode);
        this.effectifTotal = effectifTotal;
        this.effectifActif = effectifActif;
        this.tauxTurnover = tauxTurnover;
        this.tauxAbsenteisme = tauxAbsenteisme;
        this.masseSalariale = masseSalariale;
        this.couvertureCompetences = couvertureCompetences;
    }

    public static RhKpiSnapshot create(UUID tenantId, UUID organizationId, String periode,
                                       int effectifTotal, int effectifActif,
                                       BigDecimal tauxTurnover, BigDecimal tauxAbsenteisme,
                                       BigDecimal masseSalariale, BigDecimal couvertureCompetences) {
        Instant now = Instant.now();
        return new RhKpiSnapshot(UUID.randomUUID(), tenantId, now, now, organizationId, periode,
                effectifTotal, effectifActif, tauxTurnover, tauxAbsenteisme,
                masseSalariale, couvertureCompetences);
    }

    public static RhKpiSnapshot rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                          UUID organizationId, String periode, int effectifTotal,
                                          int effectifActif, BigDecimal tauxTurnover,
                                          BigDecimal tauxAbsenteisme, BigDecimal masseSalariale,
                                          BigDecimal couvertureCompetences) {
        return new RhKpiSnapshot(id, tenantId, createdAt, updatedAt, organizationId, periode,
                effectifTotal, effectifActif, tauxTurnover, tauxAbsenteisme,
                masseSalariale, couvertureCompetences);
    }

    public UUID organizationId() { return organizationId; }
    public String periode() { return periode; }
    public int effectifTotal() { return effectifTotal; }
    public int effectifActif() { return effectifActif; }
    public BigDecimal tauxTurnover() { return tauxTurnover; }
    public BigDecimal tauxAbsenteisme() { return tauxAbsenteisme; }
    public BigDecimal masseSalariale() { return masseSalariale; }
    public BigDecimal couvertureCompetences() { return couvertureCompetences; }
}
