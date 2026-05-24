package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class MissionOrder extends BaseEntity {

    private final UUID employeeId;
    private final String destination;
    private final String objet;
    private final LocalDate dateDebut;
    private final LocalDate dateFin;
    private final BigDecimal montantAvance;
    private final String centreCout;
    private final MissionOrderStatus status;

    private MissionOrder(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                         UUID employeeId, String destination, String objet,
                         LocalDate dateDebut, LocalDate dateFin, BigDecimal montantAvance,
                         String centreCout, MissionOrderStatus status) {
        super(id, tenantId, createdAt, updatedAt);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.destination = Objects.requireNonNull(destination);
        this.objet = Objects.requireNonNull(objet);
        this.dateDebut = Objects.requireNonNull(dateDebut);
        this.dateFin = Objects.requireNonNull(dateFin);
        this.status = Objects.requireNonNull(status);
        this.montantAvance = montantAvance;
        this.centreCout = centreCout;
    }

    public static MissionOrder create(UUID tenantId, UUID employeeId, String destination,
                                       String objet, LocalDate dateDebut, LocalDate dateFin,
                                       BigDecimal montantAvance, String centreCout) {
        if (dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("Mission end date must be on or after start date");
        }
        Instant now = Instant.now();
        return new MissionOrder(UUID.randomUUID(), tenantId, now, now, employeeId, destination,
                objet, dateDebut, dateFin, montantAvance, centreCout, MissionOrderStatus.DRAFT);
    }

    public static MissionOrder rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                          UUID employeeId, String destination, String objet,
                                          LocalDate dateDebut, LocalDate dateFin, BigDecimal montantAvance,
                                          String centreCout, MissionOrderStatus status) {
        return new MissionOrder(id, tenantId, createdAt, updatedAt, employeeId, destination,
                objet, dateDebut, dateFin, montantAvance, centreCout, status);
    }

    public MissionOrder approve() {
        if (this.status != MissionOrderStatus.DRAFT) throw new IllegalStateException("Cannot approve mission order in status " + this.status);
        return new MissionOrder(id(), tenantId(), createdAt(), Instant.now(), employeeId, destination,
                objet, dateDebut, dateFin, montantAvance, centreCout, MissionOrderStatus.APPROVED);
    }

    public MissionOrder start() {
        if (this.status != MissionOrderStatus.APPROVED) throw new IllegalStateException("Cannot start mission order in status " + this.status);
        return new MissionOrder(id(), tenantId(), createdAt(), Instant.now(), employeeId, destination,
                objet, dateDebut, dateFin, montantAvance, centreCout, MissionOrderStatus.IN_PROGRESS);
    }

    public MissionOrder complete() {
        if (this.status != MissionOrderStatus.IN_PROGRESS) throw new IllegalStateException("Cannot complete mission order in status " + this.status);
        return new MissionOrder(id(), tenantId(), createdAt(), Instant.now(), employeeId, destination,
                objet, dateDebut, dateFin, montantAvance, centreCout, MissionOrderStatus.COMPLETED);
    }

    public MissionOrder cancel() {
        if (this.status == MissionOrderStatus.COMPLETED || this.status == MissionOrderStatus.CANCELLED)
            throw new IllegalStateException("Cannot cancel mission order in status " + this.status);
        return new MissionOrder(id(), tenantId(), createdAt(), Instant.now(), employeeId, destination,
                objet, dateDebut, dateFin, montantAvance, centreCout, MissionOrderStatus.CANCELLED);
    }

    public UUID employeeId() { return employeeId; }
    public String destination() { return destination; }
    public String objet() { return objet; }
    public LocalDate dateDebut() { return dateDebut; }
    public LocalDate dateFin() { return dateFin; }
    public BigDecimal montantAvance() { return montantAvance; }
    public String centreCout() { return centreCout; }
    public MissionOrderStatus status() { return status; }
}
