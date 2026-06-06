package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.Contract;
import yowyob.comops.api.hrm.domain.model.Dependent;
import yowyob.comops.api.hrm.domain.model.EmergencyContact;
import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.hrm.domain.model.EmployeePersonalInfo;
import yowyob.comops.api.hrm.domain.model.LeaveBalance;

import java.time.LocalDate;
import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageEmployeeUseCase {

    Mono<Employee> createEmployee(CreateEmployeeCommand command);

    Mono<Boolean> isCnpsAvailable(String numCnps);

    Mono<Employee> updateEmployee(UUID employeeId, UpdateEmployeeCommand command);

    Mono<Employee> terminateEmployee(UUID employeeId, TerminateEmployeeCommand command);

    Mono<Employee> suspendEmployee(UUID employeeId, String reason);

    Mono<Employee> reactivateEmployee(UUID employeeId);

    Mono<Employee> getEmployee(UUID employeeId);

    Mono<EmployeeProfile> getEmployeeProfile(UUID employeeId);

    Flux<TimelineEvent> getEmployeeTimeline(UUID employeeId);

    Flux<Employee> listEmployees(UUID organizationId, UUID agencyId);

    Mono<Contract> addContract(UUID employeeId, AddContractCommand command);

    Flux<Contract> getContracts(UUID employeeId);

    Mono<Contract> getContract(UUID employeeId, UUID contractId);

    Mono<Contract> terminateContract(UUID employeeId, UUID contractId, String motif);

    Mono<Contract> renewContract(UUID employeeId, UUID contractId, LocalDate newDateFin);

    Mono<Dependent> addDependent(UUID employeeId, AddDependentCommand command);

    Flux<Dependent> getDependents(UUID employeeId);

    Flux<LeaveBalance> getLeaveBalances(UUID employeeId, int annee);

    // ── Extended personal info ────────────────────────────────────────────────

    /** Returns empty Mono if no info row exists yet. */
    Mono<EmployeePersonalInfo> getPersonalInfo(UUID employeeId);

    /** Creates or updates the personal info row for the given employee. */
    Mono<EmployeePersonalInfo> upsertPersonalInfo(UUID employeeId, UpsertPersonalInfoCommand command);

    // ── Emergency contacts ────────────────────────────────────────────────────

    Flux<EmergencyContact> getEmergencyContacts(UUID employeeId);

    Mono<EmergencyContact> addEmergencyContact(UUID employeeId, AddEmergencyContactCommand command);

    Mono<EmergencyContact> updateEmergencyContact(UUID employeeId, UUID contactId, UpdateEmergencyContactCommand command);

    Mono<Void> deleteEmergencyContact(UUID employeeId, UUID contactId);

    // ── Self-service (caller acts on their own employee record) ────────────────

    /** Resolves the caller's own employee from the request context. */
    Mono<Employee> getMyEmployee();

    Mono<EmployeePersonalInfo> upsertMyPersonalInfo(UpsertPersonalInfoCommand command);

    Mono<EmergencyContact> addMyEmergencyContact(AddEmergencyContactCommand command);

    Mono<EmergencyContact> updateMyEmergencyContact(UUID contactId, UpdateEmergencyContactCommand command);

    Mono<Void> deleteMyEmergencyContact(UUID contactId);
}
