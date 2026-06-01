package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Extended personal / contact information for an employee.
 * Stored in hrm_employee_personal_info (1:1 with hrm_employee).
 */
public final class EmployeePersonalInfo extends BaseEntity {

    private final UUID employeeId;

    // Personal identity extras
    private final String lieuNaissance;
    private final String situationMatrimoniale;
    private final String typePiece;
    private final String numeroPiece;
    private final LocalDate dateEmissionPiece;
    private final String niuFiscal;
    private final String permisConduire;
    private final String languesParlees;

    // Contact extras
    private final String emailPersonnel;
    private final String telephoneDomicile;
    private final String whatsapp;
    private final String adressePostale;
    private final String adresseDomicile;
    private final String ville;
    private final String region;
    private final String codePostal;

    private EmployeePersonalInfo(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                  UUID employeeId,
                                  String lieuNaissance, String situationMatrimoniale,
                                  String typePiece, String numeroPiece, LocalDate dateEmissionPiece,
                                  String niuFiscal, String permisConduire, String languesParlees,
                                  String emailPersonnel, String telephoneDomicile, String whatsapp,
                                  String adressePostale, String adresseDomicile, String ville,
                                  String region, String codePostal) {
        super(id, tenantId, createdAt, updatedAt);
        this.employeeId = employeeId;
        this.lieuNaissance = lieuNaissance;
        this.situationMatrimoniale = situationMatrimoniale;
        this.typePiece = typePiece;
        this.numeroPiece = numeroPiece;
        this.dateEmissionPiece = dateEmissionPiece;
        this.niuFiscal = niuFiscal;
        this.permisConduire = permisConduire;
        this.languesParlees = languesParlees;
        this.emailPersonnel = emailPersonnel;
        this.telephoneDomicile = telephoneDomicile;
        this.whatsapp = whatsapp;
        this.adressePostale = adressePostale;
        this.adresseDomicile = adresseDomicile;
        this.ville = ville;
        this.region = region;
        this.codePostal = codePostal;
    }

    public static EmployeePersonalInfo create(UUID tenantId, UUID employeeId,
                                               String lieuNaissance, String situationMatrimoniale,
                                               String typePiece, String numeroPiece, LocalDate dateEmissionPiece,
                                               String niuFiscal, String permisConduire, String languesParlees,
                                               String emailPersonnel, String telephoneDomicile, String whatsapp,
                                               String adressePostale, String adresseDomicile, String ville,
                                               String region, String codePostal) {
        Instant now = Instant.now();
        return new EmployeePersonalInfo(UUID.randomUUID(), tenantId, now, now, employeeId,
                lieuNaissance, situationMatrimoniale, typePiece, numeroPiece, dateEmissionPiece,
                niuFiscal, permisConduire, languesParlees,
                emailPersonnel, telephoneDomicile, whatsapp,
                adressePostale, adresseDomicile, ville, region, codePostal);
    }

    public static EmployeePersonalInfo rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                                  UUID employeeId,
                                                  String lieuNaissance, String situationMatrimoniale,
                                                  String typePiece, String numeroPiece, LocalDate dateEmissionPiece,
                                                  String niuFiscal, String permisConduire, String languesParlees,
                                                  String emailPersonnel, String telephoneDomicile, String whatsapp,
                                                  String adressePostale, String adresseDomicile, String ville,
                                                  String region, String codePostal) {
        return new EmployeePersonalInfo(id, tenantId, createdAt, updatedAt, employeeId,
                lieuNaissance, situationMatrimoniale, typePiece, numeroPiece, dateEmissionPiece,
                niuFiscal, permisConduire, languesParlees,
                emailPersonnel, telephoneDomicile, whatsapp,
                adressePostale, adresseDomicile, ville, region, codePostal);
    }

    public EmployeePersonalInfo update(String lieuNaissance, String situationMatrimoniale,
                                        String typePiece, String numeroPiece, LocalDate dateEmissionPiece,
                                        String niuFiscal, String permisConduire, String languesParlees,
                                        String emailPersonnel, String telephoneDomicile, String whatsapp,
                                        String adressePostale, String adresseDomicile, String ville,
                                        String region, String codePostal) {
        return new EmployeePersonalInfo(id(), tenantId(), createdAt(), Instant.now(), employeeId,
                lieuNaissance, situationMatrimoniale, typePiece, numeroPiece, dateEmissionPiece,
                niuFiscal, permisConduire, languesParlees,
                emailPersonnel, telephoneDomicile, whatsapp,
                adressePostale, adresseDomicile, ville, region, codePostal);
    }

    public UUID employeeId() { return employeeId; }
    public String lieuNaissance() { return lieuNaissance; }
    public String situationMatrimoniale() { return situationMatrimoniale; }
    public String typePiece() { return typePiece; }
    public String numeroPiece() { return numeroPiece; }
    public LocalDate dateEmissionPiece() { return dateEmissionPiece; }
    public String niuFiscal() { return niuFiscal; }
    public String permisConduire() { return permisConduire; }
    public String languesParlees() { return languesParlees; }
    public String emailPersonnel() { return emailPersonnel; }
    public String telephoneDomicile() { return telephoneDomicile; }
    public String whatsapp() { return whatsapp; }
    public String adressePostale() { return adressePostale; }
    public String adresseDomicile() { return adresseDomicile; }
    public String ville() { return ville; }
    public String region() { return region; }
    public String codePostal() { return codePostal; }
}
