package yowyob.comops.api.payroll.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * A payroll-owned employee record for tenants running payroll <em>standalone</em> (without
 * hrm-core). Carries exactly the fields {@code EmployeePayrollView} needs to drive a run,
 * plus an email used to dispatch the payslip PDF.
 *
 * Records are usually created through the CSV import, keyed by {@code (tenant, organization,
 * matricule)} — re-importing the same matricule updates the row instead of duplicating it.
 */
public final class PayrollEmployee extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID actorId; // optional — set when the employee gets a self-service account
    private final String matricule;
    private final String displayName;
    private final String email;
    private final String socialSecurityNo;
    private final int categorie;
    private final String echelon;
    private final String departmentCode;
    private final LocalDate hireDate;
    private final LocalDate departureDate;
    private final MaritalStatus maritalStatus;
    private final int dependentChildren;
    private final BigDecimal baseSalary;
    private final BigDecimal benefitsInKind;
    private final String position;
    private final PaymentChannel paymentChannel;
    private final String accountRef;
    private final boolean active;

    private PayrollEmployee(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                            UUID organizationId, UUID agencyId, UUID actorId,
                            String matricule, String displayName, String email,
                            String socialSecurityNo, int categorie, String echelon,
                            String departmentCode, LocalDate hireDate, LocalDate departureDate,
                            MaritalStatus maritalStatus, int dependentChildren,
                            BigDecimal baseSalary, BigDecimal benefitsInKind, String position,
                            PaymentChannel paymentChannel, String accountRef, boolean active) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.matricule = requireNotBlank(matricule, "matricule");
        this.displayName = requireNotBlank(displayName, "displayName");
        this.hireDate = Objects.requireNonNull(hireDate, "hireDate is required");
        this.baseSalary = Objects.requireNonNull(baseSalary, "baseSalary is required");
        this.maritalStatus = maritalStatus == null ? MaritalStatus.SINGLE : maritalStatus;
        this.paymentChannel = paymentChannel == null ? PaymentChannel.CASH : paymentChannel;
        this.agencyId = agencyId;
        this.actorId = actorId;
        this.email = email;
        this.socialSecurityNo = socialSecurityNo;
        this.categorie = categorie;
        this.echelon = echelon;
        this.departmentCode = departmentCode;
        this.departureDate = departureDate;
        this.dependentChildren = Math.max(0, dependentChildren);
        this.benefitsInKind = benefitsInKind == null ? BigDecimal.ZERO : benefitsInKind;
        this.position = position;
        this.accountRef = accountRef;
        this.active = active;
    }

    public static PayrollEmployee create(UUID tenantId, UUID organizationId, UUID agencyId,
                                         String matricule, String displayName, String email,
                                         String socialSecurityNo, int categorie, String echelon,
                                         String departmentCode, LocalDate hireDate,
                                         MaritalStatus maritalStatus, int dependentChildren,
                                         BigDecimal baseSalary, BigDecimal benefitsInKind,
                                         String position, PaymentChannel paymentChannel,
                                         String accountRef) {
        Instant now = Instant.now();
        return new PayrollEmployee(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId,
                null, matricule, displayName, email, socialSecurityNo, categorie, echelon,
                departmentCode, hireDate, null, maritalStatus, dependentChildren, baseSalary,
                benefitsInKind, position, paymentChannel, accountRef, true);
    }

    public static PayrollEmployee rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                            UUID organizationId, UUID agencyId, UUID actorId,
                                            String matricule, String displayName, String email,
                                            String socialSecurityNo, int categorie, String echelon,
                                            String departmentCode, LocalDate hireDate,
                                            LocalDate departureDate, MaritalStatus maritalStatus,
                                            int dependentChildren, BigDecimal baseSalary,
                                            BigDecimal benefitsInKind, String position,
                                            PaymentChannel paymentChannel, String accountRef,
                                            boolean active) {
        return new PayrollEmployee(id, tenantId, createdAt, updatedAt, organizationId, agencyId,
                actorId, matricule, displayName, email, socialSecurityNo, categorie, echelon,
                departmentCode, hireDate, departureDate, maritalStatus, dependentChildren,
                baseSalary, benefitsInKind, position, paymentChannel, accountRef, active);
    }

    /** Applies the mutable payroll attributes of {@code source} onto this row (CSV re-import). */
    public PayrollEmployee updateFrom(PayrollEmployee source) {
        return new PayrollEmployee(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                agencyId, actorId, matricule, source.displayName, source.email,
                source.socialSecurityNo, source.categorie, source.echelon, source.departmentCode,
                source.hireDate, source.departureDate, source.maritalStatus,
                source.dependentChildren, source.baseSalary, source.benefitsInKind, source.position,
                source.paymentChannel, source.accountRef, true);
    }

    /** Marks the employee inactive — excluded from future runs, history preserved. */
    public PayrollEmployee deactivate(LocalDate departureDate) {
        return new PayrollEmployee(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                agencyId, actorId, matricule, displayName, email, socialSecurityNo, categorie,
                echelon, departmentCode, hireDate,
                departureDate == null ? LocalDate.now() : departureDate, maritalStatus,
                dependentChildren, baseSalary, benefitsInKind, position, paymentChannel,
                accountRef, false);
    }

    private static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.strip();
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID actorId() { return actorId; }
    public String matricule() { return matricule; }
    public String displayName() { return displayName; }
    public String email() { return email; }
    public String socialSecurityNo() { return socialSecurityNo; }
    public int categorie() { return categorie; }
    public String echelon() { return echelon; }
    public String departmentCode() { return departmentCode; }
    public LocalDate hireDate() { return hireDate; }
    public LocalDate departureDate() { return departureDate; }
    public MaritalStatus maritalStatus() { return maritalStatus; }
    public int dependentChildren() { return dependentChildren; }
    public BigDecimal baseSalary() { return baseSalary; }
    public BigDecimal benefitsInKind() { return benefitsInKind; }
    public String position() { return position; }
    public PaymentChannel paymentChannel() { return paymentChannel; }
    public String accountRef() { return accountRef; }
    public boolean active() { return active; }
}
