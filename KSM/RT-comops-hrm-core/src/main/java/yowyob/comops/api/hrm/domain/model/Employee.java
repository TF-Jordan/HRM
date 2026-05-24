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

    private Employee(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                     UUID organizationId, UUID agencyId, UUID actorId, String matricule,
                     String numCnps, int categorie, String echelon, LocalDate dateEmbauche,
                     EmployeeStatus status, String departmentCode, PaymentChannel modePaiement,
                     String compteBancaire, String numMobileMoney, MobileOperator operateurMm,
                     String actorDisplayName) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.agencyId = agencyId;
        this.actorId = Objects.requireNonNull(actorId, "actorId is required");
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
    }

    public static Employee hire(UUID tenantId, UUID organizationId, UUID agencyId, UUID actorId,
                                String matricule, String numCnps, int categorie, String echelon,
                                LocalDate dateEmbauche, String departmentCode, PaymentChannel modePaiement,
                                String compteBancaire, String numMobileMoney, MobileOperator operateurMm,
                                String actorDisplayName) {
        Instant now = Instant.now();
        return new Employee(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, actorId,
                matricule, numCnps, categorie, echelon, dateEmbauche, EmployeeStatus.ACTIVE,
                departmentCode, modePaiement, compteBancaire, numMobileMoney, operateurMm, actorDisplayName);
    }

    public static Employee rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                     UUID organizationId, UUID agencyId, UUID actorId, String matricule,
                                     String numCnps, int categorie, String echelon, LocalDate dateEmbauche,
                                     EmployeeStatus status, String departmentCode, PaymentChannel modePaiement,
                                     String compteBancaire, String numMobileMoney, MobileOperator operateurMm,
                                     String actorDisplayName) {
        return new Employee(id, tenantId, createdAt, updatedAt, organizationId, agencyId, actorId,
                matricule, numCnps, categorie, echelon, dateEmbauche, status, departmentCode,
                modePaiement, compteBancaire, numMobileMoney, operateurMm, actorDisplayName);
    }

    public Employee terminate(LocalDate terminationDate, String reason) {
        if (this.status != EmployeeStatus.ACTIVE && this.status != EmployeeStatus.SUSPENDED) {
            throw new IllegalStateException("Cannot terminate employee in status " + this.status);
        }
        if (terminationDate.isBefore(dateEmbauche)) {
            throw new IllegalArgumentException("Termination date must be on or after hire date");
        }
        return new Employee(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                actorId, matricule, numCnps, categorie, echelon, dateEmbauche,
                EmployeeStatus.TERMINATED, departmentCode, modePaiement, compteBancaire,
                numMobileMoney, operateurMm, actorDisplayName);
    }

    public Employee suspend(String reason) {
        if (this.status != EmployeeStatus.ACTIVE) {
            throw new IllegalStateException("Cannot suspend employee in status " + this.status);
        }
        return new Employee(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                actorId, matricule, numCnps, categorie, echelon, dateEmbauche,
                EmployeeStatus.SUSPENDED, departmentCode, modePaiement, compteBancaire,
                numMobileMoney, operateurMm, actorDisplayName);
    }

    public Employee reactivate() {
        if (this.status != EmployeeStatus.SUSPENDED) {
            throw new IllegalStateException("Cannot reactivate employee in status " + this.status);
        }
        return new Employee(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                actorId, matricule, numCnps, categorie, echelon, dateEmbauche,
                EmployeeStatus.ACTIVE, departmentCode, modePaiement, compteBancaire,
                numMobileMoney, operateurMm, actorDisplayName);
    }

    public Employee goOnLeave() {
        if (this.status != EmployeeStatus.ACTIVE) {
            throw new IllegalStateException("Cannot set on leave employee in status " + this.status);
        }
        return new Employee(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                actorId, matricule, numCnps, categorie, echelon, dateEmbauche,
                EmployeeStatus.ON_LEAVE, departmentCode, modePaiement, compteBancaire,
                numMobileMoney, operateurMm, actorDisplayName);
    }

    public Employee returnFromLeave() {
        if (this.status != EmployeeStatus.ON_LEAVE) {
            throw new IllegalStateException("Cannot return from leave employee in status " + this.status);
        }
        return new Employee(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                actorId, matricule, numCnps, categorie, echelon, dateEmbauche,
                EmployeeStatus.ACTIVE, departmentCode, modePaiement, compteBancaire,
                numMobileMoney, operateurMm, actorDisplayName);
    }

    public Employee update(String numCnps, int categorie, String echelon, String departmentCode,
                           PaymentChannel modePaiement, String compteBancaire, String numMobileMoney,
                           MobileOperator operateurMm) {
        return new Employee(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                actorId, matricule, numCnps, categorie, echelon, dateEmbauche, status,
                departmentCode, modePaiement, compteBancaire, numMobileMoney, operateurMm, actorDisplayName);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID actorId() { return actorId; }
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
}
