package yowyob.comops.api.payroll.adapter.in.web;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.payroll.application.port.in.CreateTaxBracketTableCommand;
import yowyob.comops.api.payroll.application.port.in.ManageTaxBracketUseCase;
import yowyob.comops.api.payroll.domain.model.TaxBracket;
import yowyob.comops.api.payroll.domain.model.TaxBracketTable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Administration of the configurable progressive tax scales (IRPP…). Reads require
 * {@code hrm:payroll:read}; writes require {@code hrm:payroll:run} (the payroll manager
 * who runs payroll also configures the legal scales).
 */
@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/payroll/tax-brackets")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class TaxBracketAdminController {

    private final ManageTaxBracketUseCase useCase;

    public TaxBracketAdminController(ManageTaxBracketUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<List<TaxBracketTableResponse>>>> list(
            @RequestParam String countryCode) {
        return useCase.listTables(countryCode).map(TaxBracketTableResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Tax bracket tables fetched.")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<TaxBracketTableResponse>>> get(@PathVariable UUID id) {
        return useCase.getTable(id).map(TaxBracketTableResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Tax bracket table fetched.")));
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<TaxBracketTableResponse>>> create(
            @RequestBody Mono<CreateTaxBracketTableRequest> requestMono) {
        return requestMono.map(CreateTaxBracketTableRequest::toCommand)
                .flatMap(useCase::createTable)
                .map(TaxBracketTableResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(r, "Tax bracket table created.")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<TaxBracketTableResponse>>> deactivate(@PathVariable UUID id) {
        return useCase.deactivateTable(id).map(TaxBracketTableResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Tax bracket table deactivated.")));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<TaxBracketTableResponse>>> activate(@PathVariable UUID id) {
        return useCase.activateTable(id).map(TaxBracketTableResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Tax bracket table activated.")));
    }

    // --- DTOs ---

    public record BracketLine(int ordre, BigDecimal lowerBound, BigDecimal upperBound, BigDecimal rate) {
        TaxBracket toDomain() {
            return new TaxBracket(ordre, lowerBound, upperBound, rate);
        }

        static BracketLine from(TaxBracket b) {
            return new BracketLine(b.ordre(), b.lowerBound(), b.upperBound(), b.rate());
        }
    }

    public record CreateTaxBracketTableRequest(String code, String label, String countryCode,
            LocalDate effectiveFrom, LocalDate effectiveTo, List<BracketLine> brackets) {
        CreateTaxBracketTableCommand toCommand() {
            List<TaxBracket> domainBrackets = brackets == null ? List.of()
                    : brackets.stream().map(BracketLine::toDomain).toList();
            return new CreateTaxBracketTableCommand(code, label, countryCode, effectiveFrom, effectiveTo,
                    domainBrackets);
        }
    }

    public record TaxBracketTableResponse(UUID id, String code, String label, String countryCode,
            LocalDate effectiveFrom, LocalDate effectiveTo, boolean active, List<BracketLine> brackets) {
        static TaxBracketTableResponse from(TaxBracketTable t) {
            return new TaxBracketTableResponse(t.id(), t.code(), t.label(), t.countryCode(),
                    t.effectiveFrom(), t.effectiveTo(), t.active(),
                    t.brackets().stream().map(BracketLine::from).toList());
        }
    }
}
