package yowyob.comops.api.hrm.application.service;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.hrm.application.port.in.AddContractCommand;
import yowyob.comops.api.hrm.application.port.in.AddDependentCommand;
import yowyob.comops.api.hrm.application.port.in.AddEmergencyContactCommand;
import yowyob.comops.api.hrm.application.port.in.CreateEmployeeCommand;
import yowyob.comops.api.hrm.application.port.in.EmployeeProfile;
import yowyob.comops.api.hrm.application.port.in.ManageEmployeeUseCase;
import yowyob.comops.api.hrm.application.port.in.TerminateEmployeeCommand;
import yowyob.comops.api.hrm.application.port.in.TimelineEvent;
import yowyob.comops.api.hrm.application.port.in.UpdateEmergencyContactCommand;
import yowyob.comops.api.hrm.application.port.in.UpdateEmployeeCommand;
import yowyob.comops.api.hrm.application.port.in.UpsertPersonalInfoCommand;
import yowyob.comops.api.hrm.application.port.out.ActorPort;
import yowyob.comops.api.hrm.application.port.out.ContractRepository;
import yowyob.comops.api.hrm.application.port.out.DependentRepository;
import yowyob.comops.api.hrm.application.port.out.EmergencyContactRepository;
import yowyob.comops.api.hrm.application.port.out.EmployeePersonalInfoRepository;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.LeaveBalanceRepository;
import yowyob.comops.api.hrm.application.port.out.PerformanceReviewRepository;
import yowyob.comops.api.hrm.application.port.out.SettingsPort;
import yowyob.comops.api.hrm.application.port.out.ThirdPartyProfilePort;
import yowyob.comops.api.hrm.domain.ActiveContractAlreadyExistsException;
import yowyob.comops.api.hrm.domain.ActorNotFoundException;
import yowyob.comops.api.hrm.domain.ContractNotFoundException;
import yowyob.comops.api.hrm.domain.DuplicateCnpsException;
import yowyob.comops.api.hrm.domain.DuplicateEmployeeException;
import yowyob.comops.api.hrm.domain.EmployeeNotFoundException;
import yowyob.comops.api.hrm.domain.model.Contract;
import yowyob.comops.api.hrm.domain.model.ContractType;
import yowyob.comops.api.hrm.domain.model.Dependent;
import yowyob.comops.api.hrm.domain.model.EmergencyContact;
import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.hrm.domain.model.EmployeePersonalInfo;
import yowyob.comops.api.hrm.domain.model.LeaveBalance;
import yowyob.comops.api.hrm.domain.model.LeaveType;
import yowyob.comops.api.hrm.domain.model.MobileOperator;
import yowyob.comops.api.hrm.domain.model.PaymentChannel;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
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
    private final PerformanceReviewRepository performanceReviewRepository;
    private final EmployeePersonalInfoRepository personalInfoRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final ActorPort actorPort;
    private final SettingsPort settingsPort;
    private final ThirdPartyProfilePort thirdPartyProfilePort;
    private final BusinessEventPublisher businessEventPublisher;

    public EmployeeService(EmployeeRepository employeeRepository, ContractRepository contractRepository,
                           DependentRepository dependentRepository, LeaveBalanceRepository leaveBalanceRepository,
                           PerformanceReviewRepository performanceReviewRepository,
                           EmployeePersonalInfoRepository personalInfoRepository,
                           EmergencyContactRepository emergencyContactRepository,
                           ActorPort actorPort, SettingsPort settingsPort,
                           ThirdPartyProfilePort thirdPartyProfilePort,
                           BusinessEventPublisher businessEventPublisher) {
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.dependentRepository = dependentRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.performanceReviewRepository = performanceReviewRepository;
        this.personalInfoRepository = personalInfoRepository;
        this.emergencyContactRepository = emergencyContactRepository;
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
                        .flatMap(actorInfo -> {
                            Mono<Void> cnpsCheck = (command.numCnps() != null && !command.numCnps().isBlank())
                                    ? employeeRepository.existsByNumCnpsAndTenantId(command.numCnps(), context.tenantId())
                                            .flatMap(cnpsExists -> cnpsExists
                                                    ? Mono.error(new DuplicateCnpsException(command.numCnps()))
                                                    : Mono.empty())
                                    : Mono.empty();
                            return employeeRepository.existsByActorIdAndTenantId(command.actorId(), context.tenantId())
                                    .flatMap(exists -> exists
                                            ? Mono.error(new DuplicateEmployeeException(command.actorId()))
                                            : cnpsCheck.then(settingsPort.generateMatricule(context.tenantId(),
                                                    context.organizationId(), context.agencyId())))
                                    .flatMap(matricule -> {
                                        Employee employee = Employee.hire(
                                                context.tenantId(), context.organizationId(), context.agencyId(),
                                                command.actorId(), command.managerId(), matricule, command.numCnps(),
                                                command.categorie(), command.echelon(),
                                                command.dateEmbauche(), command.departmentCode(),
                                                PaymentChannel.valueOf(command.modePaiement()),
                                                command.compteBancaire(), command.numMobileMoney(),
                                                command.operateurMm() != null ? MobileOperator.valueOf(command.operateurMm()) : null,
                                                actorInfo.displayName());
                                        return employeeRepository.save(employee);
                                    })
                                    .flatMap(savedEmployee -> {
                                        int currentYear = LocalDate.now().getYear();
                                        Mono<Void> contractStep = command.contractType() != null
                                                ? contractRepository.save(Contract.create(
                                                        context.tenantId(), context.organizationId(), context.agencyId(),
                                                        savedEmployee.id(), ContractType.valueOf(command.contractType()),
                                                        command.position(), command.contractDateDebut(), command.contractDateFin(),
                                                        command.salaireBase(), command.avantagesNature(),
                                                        command.periodeEssai(), null)).then()
                                                : Mono.empty();
                                        return contractStep
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
                                    });
                        }));
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
                                command.operateurMm() != null ? MobileOperator.valueOf(command.operateurMm()) : null,
                                command.managerId()))
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
    public Mono<EmployeeProfile> getEmployeeProfile(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findById(context.tenantId(), employeeId)
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                        .flatMap(employee -> {
                            UUID tenantId = context.tenantId();
                            Mono<ActorPort.ActorInfo> actorMono = actorPort.resolveActor(tenantId, employee.actorId())
                                    .switchIfEmpty(Mono.fromSupplier(() ->
                                            ActorPort.ActorInfo.of(employee.actorId(), employee.actorDisplayName())));
                            Mono<String> managerNameMono = employee.managerId() == null
                                    ? Mono.just("")
                                    : employeeRepository.findById(tenantId, employee.managerId())
                                            .flatMap(manager -> actorPort.resolveActor(tenantId, manager.actorId())
                                                    .map(ActorPort.ActorInfo::displayName)
                                                    .defaultIfEmpty(manager.actorDisplayName() != null
                                                            ? manager.actorDisplayName() : ""))
                                            .defaultIfEmpty("");
                            return Mono.zip(actorMono, managerNameMono)
                                    .map(tuple -> new EmployeeProfile(employee, tuple.getT1(),
                                            tuple.getT2().isEmpty() ? null : tuple.getT2()));
                        }));
    }

    @Override
    public Flux<TimelineEvent> getEmployeeTimeline(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> {
                    UUID tenantId = context.tenantId();
                    return employeeRepository.findById(tenantId, employeeId)
                            .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                            .flatMapMany(employee -> {
                                Flux<TimelineEvent> hire = Flux.just(new TimelineEvent(
                                        "HIRE", employee.dateEmbauche(), "Embauche",
                                        "Matricule " + employee.matricule()));
                                Flux<TimelineEvent> contracts = contractRepository.findByEmployeeId(tenantId, employeeId)
                                        .map(c -> new TimelineEvent("CONTRACT", c.dateDebut(),
                                                "Contrat " + c.type().name(),
                                                "Statut " + c.status().name()));
                                Flux<TimelineEvent> reviews = performanceReviewRepository.findByEmployeeId(tenantId, employeeId)
                                        .map(r -> new TimelineEvent("REVIEW",
                                                r.createdAt().atZone(ZoneOffset.UTC).toLocalDate(),
                                                "Evaluation " + r.periode(),
                                                "Statut " + r.status().name()));
                                return Flux.concat(hire, contracts, reviews);
                            })
                            .sort(Comparator.comparing(TimelineEvent::date).reversed());
                });
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
                        // Invariant (non-negotiable): an employee may hold at most one ACTIVE
                        // contract. Reject the creation if one already exists — it must be
                        // terminated or renewed first.
                        .flatMap(employee -> contractRepository
                                .findActiveByEmployeeId(context.tenantId(), employeeId)
                                .flatMap(existing -> Mono.<Contract>error(
                                        new ActiveContractAlreadyExistsException(employeeId)))
                                .switchIfEmpty(Mono.defer(() -> {
                                    Contract contract = Contract.create(context.tenantId(), context.organizationId(),
                                            context.agencyId(), employeeId, ContractType.valueOf(command.type()),
                                            command.position(), command.dateDebut(), command.dateFin(), command.salaireBase(),
                                            command.avantagesNature(), command.periodeEssai(), command.documentFileId());
                                    return contractRepository.save(contract)
                                            .flatMap(saved -> businessEventPublisher.publish(
                                                    BusinessEvent.now(context.tenantId(), context.organizationId(),
                                                            "CONTRACT_CREATED", "CONTRACT", saved.id(),
                                                            payload("employeeId", employeeId,
                                                                    "type", saved.type().name()))).thenReturn(saved));
                                }))));
    }

    @Override
    public Flux<Contract> getContracts(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> contractRepository.findByEmployeeId(context.tenantId(), employeeId));
    }

    @Override
    public Mono<Contract> getContract(UUID employeeId, UUID contractId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> contractRepository.findById(context.tenantId(), contractId)
                        .switchIfEmpty(Mono.error(new ContractNotFoundException(contractId))));
    }

    @Override
    public Mono<Contract> terminateContract(UUID employeeId, UUID contractId, String motif) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> contractRepository.findById(context.tenantId(), contractId)
                        .switchIfEmpty(Mono.error(new ContractNotFoundException(contractId)))
                        .map(contract -> contract.terminate(motif))
                        .flatMap(contractRepository::save)
                        .flatMap(saved -> businessEventPublisher.publish(
                                BusinessEvent.now(context.tenantId(), context.organizationId(),
                                        "CONTRACT_TERMINATED", "CONTRACT", saved.id(),
                                        payload("employeeId", employeeId, "motif", motif))).thenReturn(saved)));
    }

    @Override
    public Mono<Contract> renewContract(UUID employeeId, UUID contractId, LocalDate newDateFin) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> contractRepository.findById(context.tenantId(), contractId)
                        .switchIfEmpty(Mono.error(new ContractNotFoundException(contractId)))
                        .map(contract -> contract.renew(newDateFin))
                        .flatMap(contractRepository::save)
                        .flatMap(saved -> businessEventPublisher.publish(
                                BusinessEvent.now(context.tenantId(), context.organizationId(),
                                        "CONTRACT_RENEWED", "CONTRACT", saved.id(),
                                        payload("employeeId", employeeId, "newDateFin", newDateFin))).thenReturn(saved)));
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

    @Override
    public Mono<Boolean> isCnpsAvailable(String numCnps) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.existsByNumCnpsAndTenantId(numCnps, context.tenantId())
                        .map(exists -> !exists));
    }

    // ── Extended personal info ────────────────────────────────────────────────

    @Override
    public Mono<EmployeePersonalInfo> getPersonalInfo(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> personalInfoRepository.findByEmployeeId(context.tenantId(), employeeId));
    }

    @Override
    public Mono<EmployeePersonalInfo> upsertPersonalInfo(UUID employeeId, UpsertPersonalInfoCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> {
                    UUID tenantId = context.tenantId();
                    return employeeRepository.findById(tenantId, employeeId)
                            .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                            .flatMap(employee ->
                                    personalInfoRepository.findByEmployeeId(tenantId, employeeId)
                                            .map(existing -> existing.update(
                                                    command.lieuNaissance(), command.situationMatrimoniale(),
                                                    command.typePiece(), command.numeroPiece(), command.dateEmissionPiece(),
                                                    command.niuFiscal(), command.permisConduire(), command.languesParlees(),
                                                    command.emailPersonnel(), command.telephoneDomicile(), command.whatsapp(),
                                                    command.adressePostale(), command.adresseDomicile(), command.ville(),
                                                    command.region(), command.codePostal()))
                                            .switchIfEmpty(Mono.fromSupplier(() -> EmployeePersonalInfo.create(
                                                    tenantId, employeeId,
                                                    command.lieuNaissance(), command.situationMatrimoniale(),
                                                    command.typePiece(), command.numeroPiece(), command.dateEmissionPiece(),
                                                    command.niuFiscal(), command.permisConduire(), command.languesParlees(),
                                                    command.emailPersonnel(), command.telephoneDomicile(), command.whatsapp(),
                                                    command.adressePostale(), command.adresseDomicile(), command.ville(),
                                                    command.region(), command.codePostal())))
                                            .flatMap(personalInfoRepository::save));
                });
    }

    // ── Emergency contacts ────────────────────────────────────────────────────

    @Override
    public Flux<EmergencyContact> getEmergencyContacts(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> emergencyContactRepository.findByEmployeeId(context.tenantId(), employeeId));
    }

    @Override
    public Mono<EmergencyContact> addEmergencyContact(UUID employeeId, AddEmergencyContactCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findById(context.tenantId(), employeeId)
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                        .flatMap(employee -> {
                            EmergencyContact contact = EmergencyContact.create(
                                    context.tenantId(), employeeId,
                                    command.nom(), command.prenom(), command.relation(),
                                    command.telephone(), command.email(), command.priorite());
                            return emergencyContactRepository.save(contact);
                        }));
    }

    @Override
    public Mono<EmergencyContact> updateEmergencyContact(UUID employeeId, UUID contactId,
                                                          UpdateEmergencyContactCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> emergencyContactRepository.findById(context.tenantId(), contactId)
                        .switchIfEmpty(Mono.error(new yowyob.comops.api.hrm.domain.EmergencyContactNotFoundException(contactId)))
                        .map(c -> c.update(command.nom(), command.prenom(), command.relation(),
                                command.telephone(), command.email(), command.priorite()))
                        .flatMap(emergencyContactRepository::save));
    }

    @Override
    public Mono<Void> deleteEmergencyContact(UUID employeeId, UUID contactId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> emergencyContactRepository.deleteById(context.tenantId(), contactId));
    }

    // ── Self-service (caller acts on their own employee record) ────────────────

    /** Resolves the employee linked to the calling account's actor, erroring if none. */
    private Mono<Employee> resolveMyEmployee(UUID tenantId, UUID actorId) {
        if (actorId == null) {
            return Mono.error(new EmployeeNotFoundException(null));
        }
        return employeeRepository.findByActorId(tenantId, actorId)
                .switchIfEmpty(Mono.error(new EmployeeNotFoundException(actorId)));
    }

    @Override
    public Mono<Employee> getMyEmployee() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> resolveMyEmployee(ctx.tenantId(), ctx.actorId()));
    }

    @Override
    public Mono<EmployeePersonalInfo> upsertMyPersonalInfo(UpsertPersonalInfoCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> resolveMyEmployee(ctx.tenantId(), ctx.actorId())
                        .flatMap(employee -> upsertPersonalInfo(employee.id(), command)));
    }

    @Override
    public Mono<EmergencyContact> addMyEmergencyContact(AddEmergencyContactCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> resolveMyEmployee(ctx.tenantId(), ctx.actorId())
                        .flatMap(employee -> addEmergencyContact(employee.id(), command)));
    }

    @Override
    public Mono<EmergencyContact> updateMyEmergencyContact(UUID contactId, UpdateEmergencyContactCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> resolveMyEmployee(ctx.tenantId(), ctx.actorId())
                        .flatMap(employee -> emergencyContactRepository.findById(ctx.tenantId(), contactId)
                                .switchIfEmpty(Mono.error(
                                        new yowyob.comops.api.hrm.domain.EmergencyContactNotFoundException(contactId)))
                                .flatMap(existing -> existing.employeeId().equals(employee.id())
                                        ? Mono.just(existing)
                                        : Mono.error(new yowyob.comops.api.hrm.domain.EmergencyContactNotFoundException(contactId)))
                                .map(c -> c.update(command.nom(), command.prenom(), command.relation(),
                                        command.telephone(), command.email(), command.priorite()))
                                .flatMap(emergencyContactRepository::save)));
    }

    @Override
    public Mono<Void> deleteMyEmergencyContact(UUID contactId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> resolveMyEmployee(ctx.tenantId(), ctx.actorId())
                        .flatMap(employee -> emergencyContactRepository.findById(ctx.tenantId(), contactId)
                                .switchIfEmpty(Mono.error(
                                        new yowyob.comops.api.hrm.domain.EmergencyContactNotFoundException(contactId)))
                                .flatMap(existing -> existing.employeeId().equals(employee.id())
                                        ? emergencyContactRepository.deleteById(ctx.tenantId(), contactId)
                                        : Mono.error(new yowyob.comops.api.hrm.domain.EmergencyContactNotFoundException(contactId)))));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            payload.put(entries[i].toString(), entries[i + 1]);
        }
        return payload;
    }
}
