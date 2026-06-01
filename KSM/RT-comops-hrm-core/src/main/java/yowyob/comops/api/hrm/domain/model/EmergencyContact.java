package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Emergency contact linked to an employee.
 * Stored in hrm_employee_emergency_contact (1:N with hrm_employee).
 */
public final class EmergencyContact extends BaseEntity {

    private final UUID employeeId;
    private final String nom;
    private final String prenom;
    private final String relation;
    private final String telephone;
    private final String email;
    private final int priorite;

    private EmergencyContact(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                              UUID employeeId, String nom, String prenom, String relation,
                              String telephone, String email, int priorite) {
        super(id, tenantId, createdAt, updatedAt);
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.nom = Objects.requireNonNull(nom, "nom is required");
        this.prenom = prenom;
        this.relation = relation;
        this.telephone = telephone;
        this.email = email;
        this.priorite = priorite;
    }

    public static EmergencyContact create(UUID tenantId, UUID employeeId,
                                           String nom, String prenom, String relation,
                                           String telephone, String email, int priorite) {
        Instant now = Instant.now();
        return new EmergencyContact(UUID.randomUUID(), tenantId, now, now,
                employeeId, nom, prenom, relation, telephone, email, priorite);
    }

    public static EmergencyContact rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                              UUID employeeId, String nom, String prenom, String relation,
                                              String telephone, String email, int priorite) {
        return new EmergencyContact(id, tenantId, createdAt, updatedAt,
                employeeId, nom, prenom, relation, telephone, email, priorite);
    }

    public EmergencyContact update(String nom, String prenom, String relation,
                                    String telephone, String email, int priorite) {
        return new EmergencyContact(id(), tenantId(), createdAt(), Instant.now(),
                employeeId, nom, prenom, relation, telephone, email, priorite);
    }

    public UUID employeeId() { return employeeId; }
    public String nom() { return nom; }
    public String prenom() { return prenom; }
    public String relation() { return relation; }
    public String telephone() { return telephone; }
    public String email() { return email; }
    public int priorite() { return priorite; }
}
