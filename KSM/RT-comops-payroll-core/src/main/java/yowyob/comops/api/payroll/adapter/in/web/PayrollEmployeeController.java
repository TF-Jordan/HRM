package yowyob.comops.api.payroll.adapter.in.web;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.payroll.application.port.in.ManagePayrollEmployeeUseCase;
import yowyob.comops.api.payroll.application.port.in.ManagePayrollEmployeeUseCase.CsvImportReport;
import yowyob.comops.api.payroll.application.port.in.ManagePayrollEmployeeUseCase.UpsertPayrollEmployeeCommand;
import yowyob.comops.api.payroll.application.port.out.PayrollDataSourceRepository.Source;
import yowyob.comops.api.payroll.domain.model.PayrollEmployee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Payroll-owned employee records for the <em>standalone</em> payroll mode (tenants subscribing
 * to payroll without hrm-core). The CSV import is the primary on-ramp; the data-source endpoint
 * tells the frontend which backend (HRM vs LOCAL) feeds the runs of the organization.
 *
 * Writes require {@code hrm:payroll:run}; reads {@code hrm:payroll:read} — the payroll manager
 * owns this referential.
 */
@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/payroll/employees")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class PayrollEmployeeController {

    /**
     * Canonical CSV template served to the frontend for download. The header row uses the
     * intuitive French column names; three example rows cover the three payment channels and a
     * mix of marital / dependent situations so the user can copy-and-adapt. The frontend renders
     * the {@link #templateColumns()} legend next to it for an at-a-glance "what goes where".
     */
    static final String CSV_TEMPLATE = String.join("\n",
            "matricule,nom,email,numCnps,categorie,echelon,departement,dateEmbauche,"
                    + "situationMatrimoniale,enfants,salaireBase,avantagesNature,poste,"
                    + "modePaiement,compte",
            "EMP-001,Jean Mballa,jean.mballa@example.cm,123456789,6,B,FIN,2022-01-15,"
                    + "MARRIED,2,450000,50000,Comptable,BANK_TRANSFER,CM21-10005-00001-12345678901-23",
            "EMP-002,Awa Ngono,awa.ngono@example.cm,987654321,4,A,ADM,2023-06-01,"
                    + "SINGLE,0,300000,0,Assistante,MTN_MOBILE_MONEY,677000000",
            "EMP-003,Paul Etogo,paul.etogo@example.cm,555123456,7,C,DG,2021-09-20,"
                    + "DIVORCED,1,650000,0,Directeur,CASH,") + "\n";

    /**
     * Column specification driving the on-screen legend: the canonical header, whether it is
     * required, an example value, and the closed list of accepted values when applicable. This is
     * the single source of truth for "what the payroll engine expects", so the frontend never has
     * to hard-code it. Labels and help text are localized on the frontend, keyed by {@code header}.
     */
    static List<CsvColumnSpec> templateColumns() {
        return List.of(
                new CsvColumnSpec("matricule", true, "EMP-001", List.of()),
                new CsvColumnSpec("nom", true, "Jean Mballa", List.of()),
                new CsvColumnSpec("email", false, "jean.mballa@example.cm", List.of()),
                new CsvColumnSpec("numCnps", false, "123456789", List.of()),
                new CsvColumnSpec("categorie", false, "6", List.of()),
                new CsvColumnSpec("echelon", false, "B", List.of()),
                new CsvColumnSpec("departement", false, "FIN", List.of()),
                new CsvColumnSpec("dateEmbauche", true, "2022-01-15", List.of()),
                new CsvColumnSpec("situationMatrimoniale", false, "MARRIED",
                        List.of("MARRIED", "SINGLE", "DIVORCED", "WIDOWED")),
                new CsvColumnSpec("enfants", false, "2", List.of()),
                new CsvColumnSpec("salaireBase", true, "450000", List.of()),
                new CsvColumnSpec("avantagesNature", false, "50000", List.of()),
                new CsvColumnSpec("poste", false, "Comptable", List.of()),
                new CsvColumnSpec("modePaiement", false, "BANK_TRANSFER",
                        List.of("BANK_TRANSFER", "MTN_MOBILE_MONEY", "ORANGE_MONEY", "CASH")),
                new CsvColumnSpec("compte", false, "677000000", List.of()));
    }

    private final ManagePayrollEmployeeUseCase useCase;

    public PayrollEmployeeController(ManagePayrollEmployeeUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<List<PayrollEmployeeResponse>>>> list(
            @RequestParam UUID organizationId) {
        return useCase.list(organizationId).map(PayrollEmployeeResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Payroll employees fetched.")));
    }

    @GetMapping("/{payrollEmployeeId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<PayrollEmployeeResponse>>> get(
            @PathVariable UUID payrollEmployeeId) {
        return useCase.get(payrollEmployeeId).map(PayrollEmployeeResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Payroll employee fetched.")));
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<PayrollEmployeeResponse>>> create(
            @RequestBody Mono<UpsertPayrollEmployeeRequest> requestMono) {
        return requestMono.map(UpsertPayrollEmployeeRequest::toCommand)
                .flatMap(useCase::create)
                .map(PayrollEmployeeResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(r, "Payroll employee created.")));
    }

    @PutMapping("/{payrollEmployeeId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<PayrollEmployeeResponse>>> update(
            @PathVariable UUID payrollEmployeeId,
            @RequestBody Mono<UpsertPayrollEmployeeRequest> requestMono) {
        return requestMono.map(UpsertPayrollEmployeeRequest::toCommand)
                .flatMap(cmd -> useCase.update(payrollEmployeeId, cmd))
                .map(PayrollEmployeeResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Payroll employee updated.")));
    }

    @PutMapping("/{payrollEmployeeId}/deactivate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<PayrollEmployeeResponse>>> deactivate(
            @PathVariable UUID payrollEmployeeId,
            @RequestBody(required = false) Mono<DeactivateRequest> requestMono) {
        return requestMono.defaultIfEmpty(new DeactivateRequest(null))
                .flatMap(req -> useCase.deactivate(payrollEmployeeId, req.departureDate()))
                .map(PayrollEmployeeResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Payroll employee deactivated.")));
    }

    /**
     * CSV import (header + rows, comma or semicolon separated). Body is the raw CSV text wrapped
     * in JSON ({@code {"csv": "..."}}) so the BFF can forward an uploaded file without multipart.
     */
    @PostMapping("/import")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<CsvImportReport>>> importCsv(
            @RequestParam UUID organizationId, @RequestBody Mono<CsvImportRequest> requestMono) {
        return requestMono.flatMap(req -> useCase.importCsv(organizationId, req.csv()))
                .map(report -> ResponseEntity.ok(ApiResponse.success(report, "CSV imported.")));
    }

    @GetMapping("/template")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<CsvTemplateResponse>>> template() {
        return Mono.just(ResponseEntity.ok(ApiResponse.success(
                new CsvTemplateResponse(CSV_TEMPLATE, templateColumns()), "CSV template.")));
    }

    @GetMapping("/data-source")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<DataSourceResponse>>> getDataSource(
            @RequestParam UUID organizationId) {
        return useCase.getDataSource(organizationId)
                .map(s -> ResponseEntity.ok(
                        ApiResponse.success(new DataSourceResponse(s.name()), "Data source fetched.")));
    }

    @PutMapping("/data-source")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<DataSourceResponse>>> setDataSource(
            @RequestParam UUID organizationId, @RequestBody Mono<DataSourceRequest> requestMono) {
        return requestMono
                .map(req -> Source.valueOf(req.source().toUpperCase()))
                .flatMap(source -> useCase.setDataSource(organizationId, source).thenReturn(source))
                .map(s -> ResponseEntity.ok(
                        ApiResponse.success(new DataSourceResponse(s.name()), "Data source updated.")));
    }

    // --- DTOs ---

    public record UpsertPayrollEmployeeRequest(
            UUID organizationId, UUID agencyId, String matricule, String displayName, String email,
            String socialSecurityNo, Integer categorie, String echelon, String departmentCode,
            LocalDate hireDate, String maritalStatus, Integer dependentChildren,
            BigDecimal baseSalary, BigDecimal benefitsInKind, String position,
            String paymentChannel, String accountRef) {
        UpsertPayrollEmployeeCommand toCommand() {
            return new UpsertPayrollEmployeeCommand(organizationId, agencyId, matricule, displayName,
                    email, socialSecurityNo, categorie, echelon, departmentCode, hireDate,
                    maritalStatus, dependentChildren, baseSalary, benefitsInKind, position,
                    paymentChannel, accountRef);
        }
    }

    public record DeactivateRequest(LocalDate departureDate) {}

    public record CsvImportRequest(String csv) {}

    public record CsvTemplateResponse(String csv, List<CsvColumnSpec> columns) {}

    /**
     * One CSV column the payroll engine understands. {@code header} is the canonical column name
     * (also the i18n key on the frontend); {@code acceptedValues} is empty for free-text columns.
     */
    public record CsvColumnSpec(String header, boolean required, String example,
                                List<String> acceptedValues) {}

    public record DataSourceRequest(String source) {}

    public record DataSourceResponse(String source) {}

    public record PayrollEmployeeResponse(
            UUID id, UUID organizationId, UUID agencyId, UUID actorId, String matricule,
            String displayName, String email, String socialSecurityNo, int categorie,
            String echelon, String departmentCode, LocalDate hireDate, LocalDate departureDate,
            String maritalStatus, int dependentChildren, BigDecimal baseSalary,
            BigDecimal benefitsInKind, String position, String paymentChannel, String accountRef,
            boolean active) {
        static PayrollEmployeeResponse from(PayrollEmployee e) {
            return new PayrollEmployeeResponse(e.id(), e.organizationId(), e.agencyId(), e.actorId(),
                    e.matricule(), e.displayName(), e.email(), e.socialSecurityNo(), e.categorie(),
                    e.echelon(), e.departmentCode(), e.hireDate(), e.departureDate(),
                    e.maritalStatus().name(), e.dependentChildren(), e.baseSalary(),
                    e.benefitsInKind(), e.position(), e.paymentChannel().name(), e.accountRef(),
                    e.active());
        }
    }
}
