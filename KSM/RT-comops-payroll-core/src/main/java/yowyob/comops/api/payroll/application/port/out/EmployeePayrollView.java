package yowyob.comops.api.payroll.application.port.out;

import yowyob.comops.api.payroll.domain.model.MaritalStatus;
import yowyob.comops.api.payroll.domain.model.PaymentChannel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A payroll-owned projection of everything the engine needs to pay one employee.
 *
 * This is the anti-corruption boundary with hrm-core: the integration adapter maps HR's
 * Employee + Contract + Dependants onto this record, so payroll-core never references an
 * hrm-core type. Adding/removing a field here is a deliberate contract change.
 *
 * @param employeeId       payroll's stable reference to the worker (HR employee id)
 * @param organizationId   owning organization
 * @param agencyId         owning agency, or {@code null}
 * @param actorId          user-account id, for employee self-service lookups
 * @param matricule        payroll number, shown on the payslip
 * @param displayName      human-readable name
 * @param socialSecurityNo CNPS number (or equivalent)
 * @param categorie        professional category (collective-agreement grid)
 * @param echelon          step within the category
 * @param departmentCode   cost-centre / department
 * @param hireDate         employment start, drives seniority-based elements
 * @param departureDate    employment end within the period, or {@code null}; drives proration
 * @param maritalStatus    input to the family-quotient (fiscal parts)
 * @param dependentChildren number of dependent children, input to the family quotient
 * @param baseSalary       contractual monthly base salary
 * @param benefitsInKind   valued benefits in kind (avantages en nature)
 * @param countryCode      jurisdiction selecting the applicable pay elements (e.g. "CM")
 * @param paymentChannel   how net pay is disbursed
 * @param accountRef       bank account / mobile-money number, per channel
 */
public record EmployeePayrollView(
        UUID employeeId,
        UUID organizationId,
        UUID agencyId,
        UUID actorId,
        String matricule,
        String displayName,
        String socialSecurityNo,
        int categorie,
        String echelon,
        String departmentCode,
        LocalDate hireDate,
        LocalDate departureDate,
        MaritalStatus maritalStatus,
        int dependentChildren,
        BigDecimal baseSalary,
        BigDecimal benefitsInKind,
        String countryCode,
        PaymentChannel paymentChannel,
        String accountRef) {
}
