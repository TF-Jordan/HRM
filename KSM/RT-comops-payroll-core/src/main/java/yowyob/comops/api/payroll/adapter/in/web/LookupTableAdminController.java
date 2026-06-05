package yowyob.comops.api.payroll.adapter.in.web;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.payroll.application.port.in.CreateLookupTableCommand;
import yowyob.comops.api.payroll.application.port.in.ManageLookupTableUseCase;
import yowyob.comops.api.payroll.domain.model.LookupTable;
import yowyob.comops.api.payroll.domain.model.LookupTableEntry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Administration of the configurable stepped forfait scales (RAV, TDL…). Reads require
 * {@code hrm:payroll:read}; writes require {@code hrm:payroll:run}.
 */
@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/payroll/lookup-tables")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class LookupTableAdminController {

    private final ManageLookupTableUseCase useCase;

    public LookupTableAdminController(ManageLookupTableUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<List<LookupTableResponse>>>> list(
            @RequestParam String countryCode) {
        return useCase.listTables(countryCode).map(LookupTableResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Lookup tables fetched.")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<LookupTableResponse>>> get(@PathVariable UUID id) {
        return useCase.getTable(id).map(LookupTableResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Lookup table fetched.")));
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<LookupTableResponse>>> create(
            @RequestBody Mono<CreateLookupTableRequest> requestMono) {
        return requestMono.map(CreateLookupTableRequest::toCommand)
                .flatMap(useCase::createTable)
                .map(LookupTableResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(r, "Lookup table created.")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<LookupTableResponse>>> deactivate(@PathVariable UUID id) {
        return useCase.deactivateTable(id).map(LookupTableResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Lookup table deactivated.")));
    }

    // --- DTOs ---

    public record EntryLine(int ordre, BigDecimal lowerBound, BigDecimal upperBound, BigDecimal amount) {
        LookupTableEntry toDomain() {
            return new LookupTableEntry(ordre, lowerBound, upperBound, amount);
        }

        static EntryLine from(LookupTableEntry e) {
            return new EntryLine(e.ordre(), e.lowerBound(), e.upperBound(), e.amount());
        }
    }

    public record CreateLookupTableRequest(String code, String label, String countryCode,
            LocalDate effectiveFrom, LocalDate effectiveTo, List<EntryLine> entries) {
        CreateLookupTableCommand toCommand() {
            List<LookupTableEntry> domainEntries = entries == null ? List.of()
                    : entries.stream().map(EntryLine::toDomain).toList();
            return new CreateLookupTableCommand(code, label, countryCode, effectiveFrom, effectiveTo,
                    domainEntries);
        }
    }

    public record LookupTableResponse(UUID id, String code, String label, String countryCode,
            LocalDate effectiveFrom, LocalDate effectiveTo, boolean active, List<EntryLine> entries) {
        static LookupTableResponse from(LookupTable t) {
            return new LookupTableResponse(t.id(), t.code(), t.label(), t.countryCode(),
                    t.effectiveFrom(), t.effectiveTo(), t.active(),
                    t.entries().stream().map(EntryLine::from).toList());
        }
    }
}
