package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class EmployeeSkill extends BaseEntity {

    private final UUID employeeId;
    private final UUID skillId;
    private final int niveauActuel;
    private final int niveauAttendu;
    private final LocalDate dateEvaluation;

    private EmployeeSkill(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                          UUID employeeId, UUID skillId, int niveauActuel, int niveauAttendu,
                          LocalDate dateEvaluation) {
        super(id, tenantId, createdAt, updatedAt);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.skillId = Objects.requireNonNull(skillId);
        this.niveauActuel = niveauActuel;
        this.niveauAttendu = niveauAttendu;
        this.dateEvaluation = dateEvaluation;
    }

    public static EmployeeSkill create(UUID tenantId, UUID employeeId, UUID skillId,
                                        int niveauActuel, int niveauAttendu, LocalDate dateEvaluation) {
        Instant now = Instant.now();
        return new EmployeeSkill(UUID.randomUUID(), tenantId, now, now, employeeId, skillId,
                niveauActuel, niveauAttendu, dateEvaluation);
    }

    public static EmployeeSkill rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                           UUID employeeId, UUID skillId, int niveauActuel, int niveauAttendu,
                                           LocalDate dateEvaluation) {
        return new EmployeeSkill(id, tenantId, createdAt, updatedAt, employeeId, skillId,
                niveauActuel, niveauAttendu, dateEvaluation);
    }

    public UUID employeeId() { return employeeId; }
    public UUID skillId() { return skillId; }
    public int niveauActuel() { return niveauActuel; }
    public int niveauAttendu() { return niveauAttendu; }
    public LocalDate dateEvaluation() { return dateEvaluation; }
}
