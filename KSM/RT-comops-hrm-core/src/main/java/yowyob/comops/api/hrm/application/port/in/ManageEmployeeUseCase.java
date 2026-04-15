package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.Contract;
import yowyob.comops.api.hrm.domain.model.Dependent;
import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.hrm.domain.model.LeaveBalance;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageEmployeeUseCase {

    Mono<Employee> createEmployee(CreateEmployeeCommand command);

    Mono<Employee> updateEmployee(UUID employeeId, UpdateEmployeeCommand command);

    Mono<Employee> terminateEmployee(UUID employeeId, TerminateEmployeeCommand command);

    Mono<Employee> suspendEmployee(UUID employeeId, String reason);

    Mono<Employee> reactivateEmployee(UUID employeeId);

    Mono<Employee> getEmployee(UUID employeeId);

    Flux<Employee> listEmployees(UUID organizationId, UUID agencyId);

    Mono<Contract> addContract(UUID employeeId, AddContractCommand command);

    Flux<Contract> getContracts(UUID employeeId);

    Mono<Dependent> addDependent(UUID employeeId, AddDependentCommand command);

    Flux<Dependent> getDependents(UUID employeeId);

    Flux<LeaveBalance> getLeaveBalances(UUID employeeId, int annee);
}
