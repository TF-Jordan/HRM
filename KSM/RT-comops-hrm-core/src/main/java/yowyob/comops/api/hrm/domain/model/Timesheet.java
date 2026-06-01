package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Timesheet extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID employeeId;
    private final String periode;
    private final BigDecimal heuresNormales;
    private final BigDecimal heuresSupplementaires;
    private final BigDecimal heuresNuit;
    private final BigDecimal heuresWeekend;
    private final BigDecimal absencesNonJustifiees;
    private final TimesheetStatus status;

    private Timesheet(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                      UUID organizationId, UUID agencyId, UUID employeeId, String periode,
                      BigDecimal heuresNormales, BigDecimal heuresSupplementaires,
                      BigDecimal heuresNuit, BigDecimal heuresWeekend,
                      BigDecimal absencesNonJustifiees, TimesheetStatus status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.periode = Objects.requireNonNull(periode);
        this.status = Objects.requireNonNull(status);
        this.agencyId = agencyId;
        this.heuresNormales = heuresNormales != null ? heuresNormales : BigDecimal.ZERO;
        this.heuresSupplementaires = heuresSupplementaires != null ? heuresSupplementaires : BigDecimal.ZERO;
        this.heuresNuit = heuresNuit != null ? heuresNuit : BigDecimal.ZERO;
        this.heuresWeekend = heuresWeekend != null ? heuresWeekend : BigDecimal.ZERO;
        this.absencesNonJustifiees = absencesNonJustifiees != null ? absencesNonJustifiees : BigDecimal.ZERO;
    }

    public static Timesheet create(UUID tenantId, UUID organizationId, UUID agencyId,
                                    UUID employeeId, String periode,
                                    BigDecimal heuresNormales, BigDecimal heuresSupplementaires,
                                    BigDecimal heuresNuit, BigDecimal heuresWeekend,
                                    BigDecimal absencesNonJustifiees) {
        Instant now = Instant.now();
        return new Timesheet(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId,
                employeeId, periode, heuresNormales, heuresSupplementaires, heuresNuit,
                heuresWeekend, absencesNonJustifiees, TimesheetStatus.DRAFT);
    }

    public static Timesheet rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                       UUID organizationId, UUID agencyId, UUID employeeId,
                                       String periode, BigDecimal heuresNormales,
                                       BigDecimal heuresSupplementaires, BigDecimal heuresNuit,
                                       BigDecimal heuresWeekend, BigDecimal absencesNonJustifiees,
                                       TimesheetStatus status) {
        return new Timesheet(id, tenantId, createdAt, updatedAt, organizationId, agencyId,
                employeeId, periode, heuresNormales, heuresSupplementaires, heuresNuit,
                heuresWeekend, absencesNonJustifiees, status);
    }

    public Timesheet submit() {
        if (this.status != TimesheetStatus.DRAFT) {
            throw new IllegalStateException("Cannot submit timesheet in status " + this.status);
        }
        return new Timesheet(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                employeeId, periode, heuresNormales, heuresSupplementaires, heuresNuit,
                heuresWeekend, absencesNonJustifiees, TimesheetStatus.SUBMITTED);
    }

    public Timesheet validate() {
        if (this.status != TimesheetStatus.SUBMITTED) {
            throw new IllegalStateException("Cannot validate timesheet in status " + this.status);
        }
        return new Timesheet(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                employeeId, periode, heuresNormales, heuresSupplementaires, heuresNuit,
                heuresWeekend, absencesNonJustifiees, TimesheetStatus.VALIDATED);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID employeeId() { return employeeId; }
    public String periode() { return periode; }
    public BigDecimal heuresNormales() { return heuresNormales; }
    public BigDecimal heuresSupplementaires() { return heuresSupplementaires; }
    public BigDecimal heuresNuit() { return heuresNuit; }
    public BigDecimal heuresWeekend() { return heuresWeekend; }
    public BigDecimal absencesNonJustifiees() { return absencesNonJustifiees; }
    public TimesheetStatus status() { return status; }
}
