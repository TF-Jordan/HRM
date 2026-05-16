package yowyob.comops.api.hrm.adapter.in.web;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.AddContractCommand;
import yowyob.comops.api.hrm.application.port.in.AddDependentCommand;
import yowyob.comops.api.hrm.application.port.in.CreateEmployeeCommand;
import yowyob.comops.api.hrm.application.port.in.ManageEmployeeUseCase;
import yowyob.comops.api.hrm.application.port.in.TerminateEmployeeCommand;
import yowyob.comops.api.hrm.application.port.in.UpdateEmployeeCommand;
import yowyob.comops.api.hrm.domain.model.Contract;
import yowyob.comops.api.hrm.domain.model.Dependent;
import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.hrm.domain.model.LeaveBalance;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/{employeeId}/leave-balances")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:leave:read')")
    public Mono<ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>>> getLeaveBalances(
            @PathVariable UUID employeeId, @RequestParam int annee) {
        return manageEmployeeUseCase.getLeaveBalances(employeeId, annee)
                .map(LeaveBalanceResponse::from)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Leave balances fetched.")));
    }

    // --- Request/Response DTOs ---

    public record CreateEmployeeRequest(UUID actorId, String numCnps, int categorie, String echelon,
            LocalDate dateEmbauche, String departmentCode, String modePaiement, String compteBancaire,
            String numMobileMoney, String operateurMm, String contractType, LocalDate contractDateDebut,
            LocalDate contractDateFin, BigDecimal salaireBase, BigDecimal avantagesNature, Integer periodeEssai) {
        CreateEmployeeCommand toCommand() {
            return new CreateEmployeeCommand(actorId, numCnps, categorie, echelon, dateEmbauche, departmentCode,
                    modePaiement, compteBancaire, numMobileMoney, operateurMm, contractType, contractDateDebut,
                    contractDateFin, salaireBase, avantagesNature, periodeEssai);
        }
    }

    public record UpdateEmployeeRequest(String numCnps, int categorie, String echelon, String departmentCode,
            String modePaiement, String compteBancaire, String numMobileMoney, String operateurMm) {
        UpdateEmployeeCommand toCommand() {
            return new UpdateEmployeeCommand(numCnps, categorie, echelon, departmentCode, modePaiement,
                    compteBancaire, numMobileMoney, operateurMm);
        }
    }

    public record TerminateEmployeeRequest(LocalDate terminationDate, String reason) {
        TerminateEmployeeCommand toCommand() {
            return new TerminateEmployeeCommand(terminationDate, reason);
        }
    }

    public record SuspendRequest(String reason) {}

    public record AddContractRequest(String type, LocalDate dateDebut, LocalDate dateFin,
            BigDecimal salaireBase, BigDecimal avantagesNature, Integer periodeEssai) {
        AddContractCommand toCommand() {
            return new AddContractCommand(type, dateDebut, dateFin, salaireBase, avantagesNature, periodeEssai);
        }
    }

    public record AddDependentRequest(String nom, String prenom, LocalDate dateNaissance, String lienParente) {
        AddDependentCommand toCommand() {
            return new AddDependentCommand(nom, prenom, dateNaissance, lienParente);
        }
    }

    public record EmployeeResponse(UUID id, UUID organizationId, UUID agencyId, UUID actorId, String matricule,
            String numCnps, int categorie, String echelon, LocalDate dateEmbauche, String status,
            String departmentCode, String modePaiement, String compteBancaire, String numMobileMoney,
            String operateurMm, String actorDisplayName) {
        static EmployeeResponse from(Employee e) {
            return new EmployeeResponse(e.id(), e.organizationId(), e.agencyId(), e.actorId(), e.matricule(),
                    e.numCnps(), e.categorie(), e.echelon(), e.dateEmbauche(), e.status().name(),
                    e.departmentCode(), e.modePaiement().name(), e.compteBancaire(), e.numMobileMoney(),
                    e.operateurMm() != null ? e.operateurMm().name() : null, e.actorDisplayName());
        }
    }

    public record ContractResponse(UUID id, UUID employeeId, String type, LocalDate dateDebut, LocalDate dateFin,
            BigDecimal salaireBase, BigDecimal avantagesNature, Integer periodeEssai, String status,
            String motifFin, UUID documentFileId) {
        static ContractResponse from(Contract c) {
            return new ContractResponse(c.id(), c.employeeId(), c.type().name(), c.dateDebut(), c.dateFin(),
                    c.salaireBase(), c.avantagesNature(), c.periodeEssai(), c.status().name(),
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
}
