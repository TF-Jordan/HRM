package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Employee extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID actorId;
    private final UUID managerId;
    private final String matricule;
    private final String numCnps;
    private final int categorie;
    private final String echelon;
    private final LocalDate dateEmbauche;
    private final EmployeeStatus status;
    private final String departmentCode;
    private final PaymentChannel modePaiement;
    private final String compteBancaire;
    private final String numMobileMoney;
    private final MobileOperator operateurMm;
    private final String actorDisplayName;
    private final LocalDate dateSortie;
    private final String motifSortie;

    private Employee(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                     UUID organizationId, UUID agencyId, UUID actorId, UUID managerId, String matricule,
                     String numCnps, int categorie, String echelon, LocalDate dateEmbauche,
                     EmployeeStatus status, String departmentCode, PaymentChannel modePaiement,
                     String compteBancaire, String numMobileMoney, MobileOperator operateurMm,
                     String actorDisplayName, LocalDate dateSortie, String motifSortie) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.agencyId = agencyId;
        this.actorId = Objects.requireNonNull(actorId, "actorId is required");
        this.managerId = managerId;
        this.matricule = Objects.requireNonNull(matricule, "matricule is required");
        this.numCnps = numCnps;
        this.categorie = categorie;
        this.echelon = echelon;
        this.dateEmbauche = Objects.requireNonNull(dateEmbauche, "dateEmbauche is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.departmentCode = departmentCode;
        this.modePaiement = Objects.requireNonNull(modePaiement, "modePaiement is required");
        this.compteBancaire = compteBancaire;
        this.numMobileMoney = numMobileMoney;
        this.operateurMm = operateurMm;
        this.actorDisplayName = actorDisplayName;
        this.dateSortie = dateSortie;
        this.motifSortie = motifSortie;
    }

    public static Employee hire(UUID tenantId, UUID organizationId, UUID agencyId, UUID actorId,
                                String matricule, String numCnps, int categorie, String echelon,
                                LocalDate dateEmbauche, String departmentCode, PaymentChannel modePaiement,
                                String compteBancaire, String numMobileMoney, MobileOperator operateurMm,
                                String actorDisplayName) {
        return hire(tenantId, organizationId, agencyId, actorId, null, matricule, numCnps, categorie,
                echelon, dateEmbauche, departmentCode, modePaiement, compteBancaire, numMobileMoney,
                operateurMm, actorDisplayName);
    }

    public static Employee hire(UUID tenantId, UUID organizationId, UUID agencyId, UUID actorId, UUID managerId,
                                String matricule, String numCnps, int categorie, String echelon,
                                LocalDate dateEmbauche, String departmentCode, PaymentChannel modePaiement,
                                String compteBancaire, String numMobileMoney, MobileOperator operateurMm,
                                String actorDisplayName) {
        Instant now = Instant.now();
        return new Employee(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, actorId, managerId,
                matricule, numCnps, categorie, echelon, dateEmbauche, EmployeeStatus.ACTIVE,
                departmentCode, modePaiement, compteBancaire, numMobileMoney, operateurMm, actorDisplayName,
                null, null);
    }

    public static Employee rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                     UUID organizationId, UUID agencyId, UUID actorId, UUID managerId, String matricule,
                                     String numCnps, int categorie, String echelon, LocalDate dateEmbauche,
                                     EmployeeStatus status, String departmentCode, PaymentChannel modePaiement,
                                     String compteBancaire, String numMobileMoney, MobileOperator operateurMm,
                                     String actorDisplayName) {
        return rehydrate(id, tenantId, createdAt, updatedAt, organizationId, agencyId, actorId, managerId,
                matricule, numCnps, categorie, echelon, dateEmbauche, status, departmentCode, modePaiement,
                compteBancaire, numMobileMoney, operateurMm, actorDisplayName, null, null);
    }

    public static Employee rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                     UUID organizationId, UUID agencyId, UUID actorId, UUID managerId, String matricule,
                                     String numCnps, int categorie, String echelon, LocalDate dateEmbauche,
                                     EmployeeStatus status, String departmentCode, PaymentChannel modePaiement,
                                     String compteBancaire, String numMobileMoney, MobileOperator operateurMm,
                                     String actorDisplayName, LocalDate dateSortie, String motifSortie) {
        return new Employee(id, tenantId, createdAt, updatedAt, organizationId, agencyId, actorId, managerId,
                matricule, numCnps, categorie, echelon, dateEmbauche, status, departmentCode,
                modePaiement, compteBancaire, numMobileMoney, operateurMm, actorDisplayName,
                dateSortie, motifSortie);
    }

    private Employee withStatusAndDeparture(EmployeeStatus newStatus, LocalDate newDateSortie,
                                            String newMotifSortie) {
        return new Employee(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                actorId, managerId, matricule, numCnps, categorie, echelon, dateEmbauche,
                newStatus, departmentCode, modePaiement, compteBancaire, numMobileMoney, operateurMm,
                actorDisplayName, newDateSortie, newMotifSortie);
    }

    private Employee withStatus(EmployeeStatus newStatus) {
        return withStatusAndDeparture(newStatus, dateSortie, motifSortie);
    }

    /**
     * Terminates the employment, persisting the departure date and reason — both arguments were
     * previously dropped, which made it impossible to prorate the final month's salary or build
     * a final settlement from the real exit date.
     */
    public Employee terminate(LocalDate terminationDate, String reason) {
        if (this.status != EmployeeStatus.ACTIVE && this.status != EmployeeStatus.SUSPENDED) {
            throw new IllegalStateException("Cannot terminate employee in status " + this.status);
        }
        return withStatusAndDeparture(EmployeeStatus.TERMINATED, terminationDate, reason);
    }

    public Employee suspend(String reason) {
        if (this.status != EmployeeStatus.ACTIVE) {
            throw new IllegalStateException("Cannot suspend employee in status " + this.status);
        }
        return withStatus(EmployeeStatus.SUSPENDED);
    }

    public Employee reactivate() {
        if (this.status != EmployeeStatus.SUSPENDED) {
            throw new IllegalStateException("Cannot reactivate employee in status " + this.status);
        }
        return withStatus(EmployeeStatus.ACTIVE);
    }

    public Employee goOnLeave() {
        if (this.status != EmployeeStatus.ACTIVE) {
            throw new IllegalStateException("Cannot set on leave employee in status " + this.status);
        }
        return withStatus(EmployeeStatus.ON_LEAVE);
    }

    public Employee returnFromLeave() {
        if (this.status != EmployeeStatus.ON_LEAVE) {
            throw new IllegalStateException("Cannot return from leave employee in status " + this.status);
        }
        return withStatus(EmployeeStatus.ACTIVE);
    }

    public Employee update(String numCnps, int categorie, String echelon, String departmentCode,
                           PaymentChannel modePaiement, String compteBancaire, String numMobileMoney,
                           MobileOperator operateurMm) {
        return update(numCnps, categorie, echelon, departmentCode, modePaiement, compteBancaire,
                numMobileMoney, operateurMm, this.managerId);
    }

    public Employee update(String numCnps, int categorie, String echelon, String departmentCode,
                           PaymentChannel modePaiement, String compteBancaire, String numMobileMoney,
                           MobileOperator operateurMm, UUID managerId) {
        return new Employee(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                actorId, managerId, matricule, numCnps, categorie, echelon, dateEmbauche, status,
                departmentCode, modePaiement, compteBancaire, numMobileMoney, operateurMm, actorDisplayName,
                dateSortie, motifSortie);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID actorId() { return actorId; }
    public UUID managerId() { return managerId; }
    public String matricule() { return matricule; }
    public String numCnps() { return numCnps; }
    public int categorie() { return categorie; }
    public String echelon() { return echelon; }
    public LocalDate dateEmbauche() { return dateEmbauche; }
    public EmployeeStatus status() { return status; }
    public String departmentCode() { return departmentCode; }
    public PaymentChannel modePaiement() { return modePaiement; }
    public String compteBancaire() { return compteBancaire; }
    public String numMobileMoney() { return numMobileMoney; }
    public MobileOperator operateurMm() { return operateurMm; }
    public String actorDisplayName() { return actorDisplayName; }
    public LocalDate dateSortie() { return dateSortie; }
    public String motifSortie() { return motifSortie; }
}
