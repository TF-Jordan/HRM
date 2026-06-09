package yowyob.comops.api.hrm.adapter.in.web;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.common.domain.model.ApiResponse;
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
import yowyob.comops.api.hrm.domain.ActiveContractAlreadyExistsException;
import yowyob.comops.api.hrm.domain.ContractNotFoundException;
import yowyob.comops.api.hrm.domain.DuplicateCnpsException;
import yowyob.comops.api.hrm.domain.DuplicateEmployeeException;
import yowyob.comops.api.hrm.domain.EmergencyContactNotFoundException;
import yowyob.comops.api.hrm.domain.model.Contract;
import yowyob.comops.api.hrm.domain.model.Dependent;
import yowyob.comops.api.hrm.domain.model.EmergencyContact;
import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.hrm.domain.model.EmployeePersonalInfo;
import yowyob.comops.api.hrm.domain.model.LeaveBalance;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@RestController("hrmEmployeeController")
@RequestMapping("/api/v1/hrm/employees")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class EmployeeController {

    private final ManageEmployeeUseCase manageEmployeeUseCase;

    public EmployeeController(ManageEmployeeUseCase manageEmployeeUseCase) {
        this.manageEmployeeUseCase = manageEmployeeUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:create')")
    public Mono<ResponseEntity<ApiResponse<EmployeeResponse>>> createEmployee(
            @Valid @RequestBody Mono<CreateEmployeeRequest> requestMono) {
        return requestMono.map(CreateEmployeeRequest::toCommand)
                .flatMap(manageEmployeeUseCase::createEmployee)
                .map(EmployeeResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Employee created.")));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:read')")
    public Mono<ResponseEntity<ApiResponse<EmployeeResponse>>> getEmployee(@PathVariable UUID employeeId) {
        return manageEmployeeUseCase.getEmployee(employeeId)
                .map(EmployeeResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Employee fetched.")));
    }

    @GetMapping("/{employeeId}/profile")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:read')")
    public Mono<ResponseEntity<ApiResponse<EmployeeProfileResponse>>> getEmployeeProfile(
            @PathVariable UUID employeeId) {
        return manageEmployeeUseCase.getEmployeeProfile(employeeId)
                .map(EmployeeProfileResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Employee profile fetched.")));
    }

    @GetMapping("/{employeeId}/timeline")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:read')")
    public Mono<ResponseEntity<ApiResponse<List<TimelineEventResponse>>>> getEmployeeTimeline(
            @PathVariable UUID employeeId) {
        return manageEmployeeUseCase.getEmployeeTimeline(employeeId)
                .map(TimelineEventResponse::from)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Employee timeline fetched.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:read')")
    public Mono<ResponseEntity<ApiResponse<List<EmployeeResponse>>>> listEmployees(
            @RequestParam UUID organizationId,
            @RequestParam(required = false) UUID agencyId) {
        return manageEmployeeUseCase.listEmployees(organizationId, agencyId)
                .map(EmployeeResponse::from)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Employees fetched.")));
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:update')")
    public Mono<ResponseEntity<ApiResponse<EmployeeResponse>>> updateEmployee(
            @PathVariable UUID employeeId,
            @Valid @RequestBody Mono<UpdateEmployeeRequest> requestMono) {
        return requestMono.map(UpdateEmployeeRequest::toCommand)
                .flatMap(cmd -> manageEmployeeUseCase.updateEmployee(employeeId, cmd))
                .map(EmployeeResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Employee updated.")));
    }

    @PutMapping("/{employeeId}/terminate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:terminate')")
    public Mono<ResponseEntity<ApiResponse<EmployeeResponse>>> terminateEmployee(
            @PathVariable UUID employeeId,
            @Valid @RequestBody Mono<TerminateEmployeeRequest> requestMono) {
        return requestMono.map(TerminateEmployeeRequest::toCommand)
                .flatMap(cmd -> manageEmployeeUseCase.terminateEmployee(employeeId, cmd))
                .map(EmployeeResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Employee terminated.")));
    }

    @PutMapping("/{employeeId}/suspend")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:suspend')")
    public Mono<ResponseEntity<ApiResponse<EmployeeResponse>>> suspendEmployee(
            @PathVariable UUID employeeId, @RequestBody Mono<SuspendRequest> requestMono) {
        return requestMono.flatMap(req -> manageEmployeeUseCase.suspendEmployee(employeeId, req.reason()))
                .map(EmployeeResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Employee suspended.")));
    }

    @PutMapping("/{employeeId}/reactivate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:reactivate')")
    public Mono<ResponseEntity<ApiResponse<EmployeeResponse>>> reactivateEmployee(@PathVariable UUID employeeId) {
        return manageEmployeeUseCase.reactivateEmployee(employeeId)
                .map(EmployeeResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Employee reactivated.")));
    }

    @PostMapping("/{employeeId}/contracts")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:contract:create')")
    public Mono<ResponseEntity<ApiResponse<ContractResponse>>> addContract(
            @PathVariable UUID employeeId,
            @Valid @RequestBody Mono<AddContractRequest> requestMono) {
        return requestMono.map(AddContractRequest::toCommand)
                .flatMap(cmd -> manageEmployeeUseCase.addContract(employeeId, cmd))
                .map(ContractResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Contract created.")));
    }

    @GetMapping("/{employeeId}/contracts")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:contract:read')")
    public Mono<ResponseEntity<ApiResponse<List<ContractResponse>>>> getContracts(@PathVariable UUID employeeId) {
        return manageEmployeeUseCase.getContracts(employeeId)
                .map(ContractResponse::from)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Contracts fetched.")));
    }

    @GetMapping("/{employeeId}/contracts/{contractId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:contract:read')")
    public Mono<ResponseEntity<ApiResponse<ContractResponse>>> getContract(
            @PathVariable UUID employeeId, @PathVariable UUID contractId) {
        return manageEmployeeUseCase.getContract(employeeId, contractId)
                .map(ContractResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Contract fetched.")));
    }

    @PutMapping("/{employeeId}/contracts/{contractId}/terminate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:contract:update')")
    public Mono<ResponseEntity<ApiResponse<ContractResponse>>> terminateContract(
            @PathVariable UUID employeeId, @PathVariable UUID contractId,
            @RequestBody Mono<TerminateContractRequest> requestMono) {
        return requestMono.flatMap(req -> manageEmployeeUseCase.terminateContract(employeeId, contractId, req.motif()))
                .map(ContractResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Contract terminated.")));
    }

    @PostMapping("/{employeeId}/contracts/{contractId}/renew")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:contract:update')")
    public Mono<ResponseEntity<ApiResponse<ContractResponse>>> renewContract(
            @PathVariable UUID employeeId, @PathVariable UUID contractId,
            @RequestBody Mono<RenewContractRequest> requestMono) {
        return requestMono.flatMap(req -> manageEmployeeUseCase.renewContract(employeeId, contractId, req.newDateFin()))
                .map(ContractResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Contract renewed.")));
    }

    @PostMapping("/{employeeId}/dependents")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:dependent:create')")
    public Mono<ResponseEntity<ApiResponse<DependentResponse>>> addDependent(
            @PathVariable UUID employeeId,
            @Valid @RequestBody Mono<AddDependentRequest> requestMono) {
        return requestMono.map(AddDependentRequest::toCommand)
                .flatMap(cmd -> manageEmployeeUseCase.addDependent(employeeId, cmd))
                .map(DependentResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Dependent added.")));
    }

    @GetMapping("/{employeeId}/dependents")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:dependent:read')")
    public Mono<ResponseEntity<ApiResponse<List<DependentResponse>>>> getDependents(@PathVariable UUID employeeId) {
        return manageEmployeeUseCase.getDependents(employeeId)
                .map(DependentResponse::from)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Dependents fetched.")));
    }

    // ── Personal info ─────────────────────────────────────────────────────────

    @GetMapping("/{employeeId}/personal-info")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:read')")
    public Mono<ResponseEntity<ApiResponse<PersonalInfoResponse>>> getPersonalInfo(
            @PathVariable UUID employeeId) {
        return manageEmployeeUseCase.getPersonalInfo(employeeId)
                .map(PersonalInfoResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Personal info fetched.")))
                .defaultIfEmpty(ResponseEntity.ok(ApiResponse.success(null, "No personal info yet.")));
    }

    @PutMapping("/{employeeId}/personal-info")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:update')")
    public Mono<ResponseEntity<ApiResponse<PersonalInfoResponse>>> upsertPersonalInfo(
            @PathVariable UUID employeeId,
            @Valid @RequestBody Mono<UpsertPersonalInfoRequest> requestMono) {
        return requestMono.map(UpsertPersonalInfoRequest::toCommand)
                .flatMap(cmd -> manageEmployeeUseCase.upsertPersonalInfo(employeeId, cmd))
                .map(PersonalInfoResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Personal info saved.")));
    }

    // ── Emergency contacts ────────────────────────────────────────────────────

    @GetMapping("/{employeeId}/emergency-contacts")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:read')")
    public Mono<ResponseEntity<ApiResponse<List<EmergencyContactResponse>>>> getEmergencyContacts(
            @PathVariable UUID employeeId) {
        return manageEmployeeUseCase.getEmergencyContacts(employeeId)
                .map(EmergencyContactResponse::from)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Emergency contacts fetched.")));
    }

    @PostMapping("/{employeeId}/emergency-contacts")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:update')")
    public Mono<ResponseEntity<ApiResponse<EmergencyContactResponse>>> addEmergencyContact(
            @PathVariable UUID employeeId,
            @Valid @RequestBody Mono<AddEmergencyContactRequest> requestMono) {
        return requestMono.map(AddEmergencyContactRequest::toCommand)
                .flatMap(cmd -> manageEmployeeUseCase.addEmergencyContact(employeeId, cmd))
                .map(EmergencyContactResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(r, "Emergency contact added.")));
    }

    @PatchMapping("/{employeeId}/emergency-contacts/{contactId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:update')")
    public Mono<ResponseEntity<ApiResponse<EmergencyContactResponse>>> updateEmergencyContact(
            @PathVariable UUID employeeId, @PathVariable UUID contactId,
            @Valid @RequestBody Mono<UpdateEmergencyContactRequest> requestMono) {
        return requestMono.map(UpdateEmergencyContactRequest::toCommand)
                .flatMap(cmd -> manageEmployeeUseCase.updateEmergencyContact(employeeId, contactId, cmd))
                .map(EmergencyContactResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Emergency contact updated.")));
    }

    @DeleteMapping("/{employeeId}/emergency-contacts/{contactId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:update')")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteEmergencyContact(
            @PathVariable UUID employeeId, @PathVariable UUID contactId) {
        return manageEmployeeUseCase.deleteEmergencyContact(employeeId, contactId)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success(null, "Emergency contact deleted."))));
    }

    @GetMapping("/{employeeId}/leave-balances")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:leave:read')")
    public Mono<ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>>> getLeaveBalances(
            @PathVariable UUID employeeId, @RequestParam int annee) {
        return manageEmployeeUseCase.getLeaveBalances(employeeId, annee)
                .map(LeaveBalanceResponse::from)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Leave balances fetched.")));
    }

    @GetMapping("/check-cnps")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:employee:create')")
    public Mono<ResponseEntity<ApiResponse<Boolean>>> checkCnpsAvailability(@RequestParam String value) {
        return manageEmployeeUseCase.isCnpsAvailable(value)
                .map(available -> ResponseEntity.ok(ApiResponse.success(available, "CNPS availability checked.")));
    }

    // --- Request/Response DTOs ---

    public record CreateEmployeeRequest(UUID actorId, UUID managerId, String numCnps, int categorie, String echelon,
            LocalDate dateEmbauche, String departmentCode, String modePaiement, String compteBancaire,
            String numMobileMoney, String operateurMm, String contractType, String position, LocalDate contractDateDebut,
            LocalDate contractDateFin, BigDecimal salaireBase, BigDecimal avantagesNature, Integer periodeEssai) {
        CreateEmployeeCommand toCommand() {
            return new CreateEmployeeCommand(actorId, managerId, numCnps, categorie, echelon, dateEmbauche, departmentCode,
                    modePaiement, compteBancaire, numMobileMoney, operateurMm, contractType, position, contractDateDebut,
                    contractDateFin, salaireBase, avantagesNature, periodeEssai);
        }
    }

    public record UpdateEmployeeRequest(String numCnps, int categorie, String echelon, String departmentCode,
            String modePaiement, String compteBancaire, String numMobileMoney, String operateurMm, UUID managerId) {
        UpdateEmployeeCommand toCommand() {
            return new UpdateEmployeeCommand(numCnps, categorie, echelon, departmentCode, modePaiement,
                    compteBancaire, numMobileMoney, operateurMm, managerId);
        }
    }

    public record TerminateEmployeeRequest(LocalDate terminationDate, String reason) {
        TerminateEmployeeCommand toCommand() {
            return new TerminateEmployeeCommand(terminationDate, reason);
        }
    }

    public record SuspendRequest(String reason) {}

    public record AddContractRequest(String type, String position, LocalDate dateDebut, LocalDate dateFin,
            BigDecimal salaireBase, BigDecimal avantagesNature, Integer periodeEssai, UUID documentFileId) {
        AddContractCommand toCommand() {
            return new AddContractCommand(type, position, dateDebut, dateFin, salaireBase, avantagesNature, periodeEssai, documentFileId);
        }
    }

    public record TerminateContractRequest(String motif) {}

    public record RenewContractRequest(LocalDate newDateFin) {}

    public record AddDependentRequest(String nom, String prenom, LocalDate dateNaissance, String lienParente) {
        AddDependentCommand toCommand() {
            return new AddDependentCommand(nom, prenom, dateNaissance, lienParente);
        }
    }

    public record EmployeeResponse(UUID id, UUID organizationId, UUID agencyId, UUID actorId, UUID managerId,
            String matricule, String numCnps, int categorie, String echelon, LocalDate dateEmbauche, String status,
            String departmentCode, String modePaiement, String compteBancaire, String numMobileMoney,
            String operateurMm, String actorDisplayName) {
        static EmployeeResponse from(Employee e) {
            return new EmployeeResponse(e.id(), e.organizationId(), e.agencyId(), e.actorId(), e.managerId(),
                    e.matricule(), e.numCnps(), e.categorie(), e.echelon(), e.dateEmbauche(), e.status().name(),
                    e.departmentCode(), e.modePaiement().name(), e.compteBancaire(), e.numMobileMoney(),
                    e.operateurMm() != null ? e.operateurMm().name() : null, e.actorDisplayName());
        }
    }

    public record ContractResponse(UUID id, UUID employeeId, String type, String position, LocalDate dateDebut,
            LocalDate dateFin, BigDecimal salaireBase, BigDecimal avantagesNature, Integer periodeEssai, String status,
            String motifFin, UUID documentFileId) {
        static ContractResponse from(Contract c) {
            return new ContractResponse(c.id(), c.employeeId(), c.type().name(), c.position(), c.dateDebut(),
                    c.dateFin(), c.salaireBase(), c.avantagesNature(), c.periodeEssai(), c.status().name(),
                    c.motifFin(), c.documentFileId());
        }
    }

    public record DependentResponse(UUID id, UUID employeeId, String nom, String prenom, LocalDate dateNaissance,
            String lienParente, UUID certificatFileId) {
        static DependentResponse from(Dependent d) {
            return new DependentResponse(d.id(), d.employeeId(), d.nom(), d.prenom(), d.dateNaissance(),
                    d.lienParente(), d.certificatFileId());
        }
    }

    public record LeaveBalanceResponse(UUID id, UUID employeeId, String type, BigDecimal acquis, BigDecimal pris,
            BigDecimal soldeRestant, int annee) {
        static LeaveBalanceResponse from(LeaveBalance lb) {
            return new LeaveBalanceResponse(lb.id(), lb.employeeId(), lb.type().name(), lb.acquis(), lb.pris(),
                    lb.soldeRestant(), lb.annee());
        }
    }

    public record EmployeeProfileResponse(
            UUID id, UUID organizationId, UUID agencyId, UUID actorId, UUID managerId,
            String matricule, String numCnps, int categorie, String echelon,
            LocalDate dateEmbauche, String status, String departmentCode,
            String modePaiement, String compteBancaire, String numMobileMoney,
            String operateurMm, String actorDisplayName,
            String actorFirstName, String actorLastName, String actorEmail,
            String actorPhoneNumber, String actorGender, String actorNationality,
            LocalDate actorBirthDate, String actorPhotoUri,
            String managerDisplayName) {
        static EmployeeProfileResponse from(yowyob.comops.api.hrm.application.port.in.EmployeeProfile p) {
            Employee e = p.employee();
            yowyob.comops.api.hrm.application.port.out.ActorPort.ActorInfo a = p.actor();
            return new EmployeeProfileResponse(
                    e.id(), e.organizationId(), e.agencyId(), e.actorId(), e.managerId(),
                    e.matricule(), e.numCnps(), e.categorie(), e.echelon(), e.dateEmbauche(),
                    e.status().name(), e.departmentCode(),
                    e.modePaiement().name(), e.compteBancaire(), e.numMobileMoney(),
                    e.operateurMm() != null ? e.operateurMm().name() : null,
                    e.actorDisplayName(),
                    a != null ? a.firstName() : null,
                    a != null ? a.lastName() : null,
                    a != null ? a.email() : null,
                    a != null ? a.phoneNumber() : null,
                    a != null ? a.gender() : null,
                    a != null ? a.nationality() : null,
                    a != null ? a.birthDate() : null,
                    a != null ? a.photoUri() : null,
                    p.managerDisplayName());
        }
    }

    public record PersonalInfoResponse(
            UUID id, UUID employeeId,
            String lieuNaissance, String situationMatrimoniale,
            String typePiece, String numeroPiece, LocalDate dateEmissionPiece,
            String niuFiscal, String permisConduire, String languesParlees,
            String emailPersonnel, String telephoneDomicile, String whatsapp,
            String adressePostale, String adresseDomicile, String ville,
            String region, String codePostal) {
        static PersonalInfoResponse from(EmployeePersonalInfo i) {
            return new PersonalInfoResponse(
                    i.id(), i.employeeId(),
                    i.lieuNaissance(), i.situationMatrimoniale(),
                    i.typePiece(), i.numeroPiece(), i.dateEmissionPiece(),
                    i.niuFiscal(), i.permisConduire(), i.languesParlees(),
                    i.emailPersonnel(), i.telephoneDomicile(), i.whatsapp(),
                    i.adressePostale(), i.adresseDomicile(), i.ville(),
                    i.region(), i.codePostal());
        }
    }

    public record EmergencyContactResponse(
            UUID id, UUID employeeId,
            String nom, String prenom, String relation,
            String telephone, String email, int priorite) {
        static EmergencyContactResponse from(EmergencyContact c) {
            return new EmergencyContactResponse(
                    c.id(), c.employeeId(),
                    c.nom(), c.prenom(), c.relation(),
                    c.telephone(), c.email(), c.priorite());
        }
    }

    public record UpsertPersonalInfoRequest(
            String lieuNaissance, String situationMatrimoniale,
            String typePiece, String numeroPiece, LocalDate dateEmissionPiece,
            String niuFiscal, String permisConduire, String languesParlees,
            String emailPersonnel, String telephoneDomicile, String whatsapp,
            String adressePostale, String adresseDomicile, String ville,
            String region, String codePostal) {
        UpsertPersonalInfoCommand toCommand() {
            return new UpsertPersonalInfoCommand(
                    lieuNaissance, situationMatrimoniale, typePiece, numeroPiece, dateEmissionPiece,
                    niuFiscal, permisConduire, languesParlees,
                    emailPersonnel, telephoneDomicile, whatsapp,
                    adressePostale, adresseDomicile, ville, region, codePostal);
        }
    }

    public record AddEmergencyContactRequest(
            String nom, String prenom, String relation,
            String telephone, String email, int priorite) {
        AddEmergencyContactCommand toCommand() {
            return new AddEmergencyContactCommand(nom, prenom, relation, telephone, email, priorite);
        }
    }

    public record UpdateEmergencyContactRequest(
            String nom, String prenom, String relation,
            String telephone, String email, int priorite) {
        UpdateEmergencyContactCommand toCommand() {
            return new UpdateEmergencyContactCommand(nom, prenom, relation, telephone, email, priorite);
        }
    }

    public record TimelineEventResponse(String type, LocalDate date, String title, String detail) {
        static TimelineEventResponse from(yowyob.comops.api.hrm.application.port.in.TimelineEvent e) {
            return new TimelineEventResponse(e.type(), e.date(), e.title(), e.detail());
        }
    }

    @RestControllerAdvice(assignableTypes = EmployeeController.class)
    static class HrmEmployeeExceptionHandler {

        @ExceptionHandler(DuplicateEmployeeException.class)
        ResponseEntity<ApiResponse<Void>> handleDuplicateEmployee(DuplicateEmployeeException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.failure(ex.getMessage(), "DUPLICATE_EMPLOYEE"));
        }

        @ExceptionHandler(DuplicateCnpsException.class)
        ResponseEntity<ApiResponse<Void>> handleDuplicateCnps(DuplicateCnpsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.failure(ex.getMessage(), "DUPLICATE_CNPS"));
        }

        @ExceptionHandler(ContractNotFoundException.class)
        ResponseEntity<ApiResponse<Void>> handleContractNotFound(ContractNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(ex.getMessage(), "CONTRACT_NOT_FOUND"));
        }

        @ExceptionHandler(ActiveContractAlreadyExistsException.class)
        ResponseEntity<ApiResponse<Void>> handleActiveContractExists(ActiveContractAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.failure(ex.getMessage(), "ACTIVE_CONTRACT_EXISTS"));
        }
    }
}
