package yowyob.comops.api.hrm.application.service;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.hrm.application.port.in.AddContractCommand;
import yowyob.comops.api.hrm.application.port.in.AddDependentCommand;
import yowyob.comops.api.hrm.application.port.in.CreateEmployeeCommand;
import yowyob.comops.api.hrm.application.port.in.ManageEmployeeUseCase;
import yowyob.comops.api.hrm.application.port.in.TerminateEmployeeCommand;
import yowyob.comops.api.hrm.application.port.in.UpdateEmployeeCommand;
import yowyob.comops.api.hrm.application.port.out.ActorPort;
import yowyob.comops.api.hrm.application.port.out.ContractRepository;
import yowyob.comops.api.hrm.application.port.out.DependentRepository;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.LeaveBalanceRepository;
import yowyob.comops.api.hrm.application.port.out.SettingsPort;
import yowyob.comops.api.hrm.application.port.out.ThirdPartyProfilePort;
import yowyob.comops.api.hrm.domain.ActorNotFoundException;
import yowyob.comops.api.hrm.domain.DuplicateEmployeeException;
import yowyob.comops.api.hrm.domain.EmployeeNotFoundException;
import yowyob.comops.api.hrm.domain.model.Contract;
import yowyob.comops.api.hrm.domain.model.ContractType;
import yowyob.comops.api.hrm.domain.model.Dependent;
import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.hrm.domain.model.LeaveBalance;
import yowyob.comops.api.hrm.domain.model.LeaveType;
import yowyob.comops.api.hrm.domain.model.MobileOperator;
import yowyob.comops.api.hrm.domain.model.PaymentChannel;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@Service
public class EmployeeService implements ManageEmployeeUseCase {

    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final DependentRepository dependentRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final ActorPort actorPort;
    private final SettingsPort settingsPort;
    private final ThirdPartyProfilePort thirdPartyProfilePort;
    private final BusinessEventPublisher businessEventPublisher;

    public EmployeeService(EmployeeRepository employeeRepository, ContractRepository contractRepository,
                           DependentRepository dependentRepository, LeaveBalanceRepository leaveBalanceRepository,
                           ActorPort actorPort, SettingsPort settingsPort,
                           ThirdPartyProfilePort thirdPartyProfilePort,
                           BusinessEventPublisher businessEventPublisher) {
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.dependentRepository = dependentRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.actorPort = actorPort;
        this.settingsPort = settingsPort;
        this.thirdPartyProfilePort = thirdPartyProfilePort;
        this.businessEventPublisher = businessEventPublisher;
    }

    @Override
    public Mono<Employee> createEmployee(CreateEmployeeCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> actorPort.resolveActor(context.tenantId(), command.actorId())
                        .switchIfEmpty(Mono.error(new ActorNotFoundException(command.actorId())))
                        .flatMap(actorInfo -> employeeRepository.existsByActorIdAndTenantId(command.actorId(), context.tenantId())
                                .flatMap(exists -> exists
                                        ? Mono.error(new DuplicateEmployeeException(command.actorId()))
                                        : settingsPort.generateMatricule(context.tenantId(), context.organizationId(),
                                                context.agencyId())
                                                .flatMap(matricule -> {
                                                    Employee employee = Employee.hire(
                                                            context.tenantId(), context.organizationId(), context.agencyId(),
                                                            command.actorId(), matricule, command.numCnps(),
                                                            command.categorie(), command.echelon(),
                                                            command.dateEmbauche(), command.departmentCode(),
                                                            PaymentChannel.valueOf(command.modePaiement()),
                                                            command.compteBancaire(), command.numMobileMoney(),
                                                            command.operateurMm() != null ? MobileOperator.valueOf(command.operateurMm()) : null,
                                                            actorInfo.displayName());
                                                    return employeeRepository.save(employee);
                                                })
                                                .flatMap(savedEmployee -> {
                                                    Contract contract = Contract.create(
                                                            context.tenantId(), context.organizationId(), context.agencyId(),
                                                            savedEmployee.id(), ContractType.valueOf(command.contractType()),
                                                            command.contractDateDebut(), command.contractDateFin(),
                                                            command.salaireBase(), command.avantagesNature(),
                                                            command.periodeEssai());
                                                    int currentYear = LocalDate.now().getYear();
                                                    return contractRepository.save(contract)
                                                            .then(leaveBalanceRepository.save(
                                                                    LeaveBalance.initialize(context.tenantId(), context.organizationId(),
                                                                            savedEmployee.id(), LeaveType.ANNUAL, currentYear)))
                                                            .then(leaveBalanceRepository.save(
                                                                    LeaveBalance.initialize(context.tenantId(), context.organizationId(),
                                                                            savedEmployee.id(), LeaveType.SICK, currentYear)))
                                                            .then(thirdPartyProfilePort.ensureEmployeeFinancialProfile(
                                                                    context.tenantId(), context.organizationId(),
                                                                    savedEmployee.actorId(), savedEmployee.matricule(),
                                                                    savedEmployee.actorDisplayName()))
                                                            .then(businessEventPublisher.publish(
                                                                    BusinessEvent.now(context.tenantId(), context.organizationId(),
                                                                            "EMPLOYEE_CREATED", "EMPLOYEE", savedEmployee.id(),
                                                                            payload("matricule", savedEmployee.matricule(),
                                                                                    "actorId", savedEmployee.actorId(),
                                                                                    "status", savedEmployee.status().name()))))
                                                            .thenReturn(savedEmployee);
                                                }))));
    }

    @Override
    public Mono<Employee> updateEmployee(UUID employeeId, UpdateEmployeeCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findById(context.tenantId(), employeeId)
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                        .map(employee -> employee.update(
                                command.numCnps(), command.categorie(), command.echelon(),
                                command.departmentCode(), PaymentChannel.valueOf(command.modePaiement()),
                                command.compteBancaire(), command.numMobileMoney(),
                                command.operateurMm() != null ? MobileOperator.valueOf(command.operateurMm()) : null))
                        .flatMap(employeeRepository::save)
                        .flatMap(saved -> businessEventPublisher.publish(
                                BusinessEvent.now(context.tenantId(), context.organizationId(),
                                        "EMPLOYEE_UPDATED", "EMPLOYEE", saved.id(),
                                        payload("matricule", saved.matricule()))).thenReturn(saved)));
    }

    @Override
    public Mono<Employee> terminateEmployee(UUID employeeId, TerminateEmployeeCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findById(context.tenantId(), employeeId)
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                        .map(employee -> employee.terminate(command.terminationDate(), command.reason()))
                        .flatMap(employeeRepository::save)
                        .flatMap(saved -> contractRepository.findActiveByEmployeeId(context.tenantId(), employeeId)
                                .flatMap(contract -> contractRepository.save(contract.terminate(command.reason())))
                                .then(Mono.just(saved))));
    }

    @Override
    public Mono<Employee> suspendEmployee(UUID employeeId, String reason) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findById(context.tenantId(), employeeId)
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                        .map(employee -> employee.suspend(reason))
                        .flatMap(employeeRepository::save));
    }

    @Override
    public Mono<Employee> reactivateEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findById(context.tenantId(), employeeId)
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                        .map(Employee::reactivate)
                        .flatMap(employeeRepository::save));
    }

    @Override
    public Mono<Employee> getEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findById(context.tenantId(), employeeId)
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId))));
    }

    @Override
    public Flux<Employee> listEmployees(UUID organizationId, UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> agencyId != null
                        ? employeeRepository.findByOrganizationIdAndAgencyId(context.tenantId(), organizationId, agencyId)
                        : employeeRepository.findByOrganizationId(context.tenantId(), organizationId));
    }

    @Override
    public Mono<Contract> addContract(UUID employeeId, AddContractCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findById(context.tenantId(), employeeId)
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                        .flatMap(employee -> {
                            Contract contract = Contract.create(context.tenantId(), context.organizationId(),
                                    context.agencyId(), employeeId, ContractType.valueOf(command.type()),
                                    command.dateDebut(), command.dateFin(), command.salaireBase(),
                                    command.avantagesNature(), command.periodeEssai());
                            return contractRepository.save(contract)
                                    .flatMap(saved -> businessEventPublisher.publish(
                                            BusinessEvent.now(context.tenantId(), context.organizationId(),
                                                    "CONTRACT_CREATED", "CONTRACT", saved.id(),
                                                    payload("employeeId", employeeId,
                                                            "type", saved.type().name()))).thenReturn(saved));
                        }));
    }

    @Override
    public Flux<Contract> getContracts(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> contractRepository.findByEmployeeId(context.tenantId(), employeeId));
    }

    @Override
    public Mono<Dependent> addDependent(UUID employeeId, AddDependentCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findById(context.tenantId(), employeeId)
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                        .flatMap(employee -> {
                            Dependent dependent = Dependent.create(context.tenantId(), context.organizationId(),
                                    employeeId, command.nom(), command.prenom(), command.dateNaissance(),
                                    command.lienParente());
                            return dependentRepository.save(dependent);
                        }));
    }

    @Override
    public Flux<Dependent> getDependents(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> dependentRepository.findByEmployeeId(context.tenantId(), employeeId));
    }

    @Override
    public Flux<LeaveBalance> getLeaveBalances(UUID employeeId, int annee) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> leaveBalanceRepository.findByEmployeeIdAndAnnee(context.tenantId(), employeeId, annee));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            payload.put(entries[i].toString(), entries[i + 1]);
        }
        return payload;
    }
}
