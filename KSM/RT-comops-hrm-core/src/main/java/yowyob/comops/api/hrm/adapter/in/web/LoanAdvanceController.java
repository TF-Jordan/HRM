package yowyob.comops.api.hrm.adapter.in.web;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.ManageLoanAdvanceUseCase;
import yowyob.comops.api.hrm.application.port.in.RequestLoanAdvanceCommand;
import yowyob.comops.api.hrm.domain.model.LoanAdvance;

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
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/hrm/loan-advances")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class LoanAdvanceController {

    private final ManageLoanAdvanceUseCase manageLoanAdvanceUseCase;

    public LoanAdvanceController(ManageLoanAdvanceUseCase manageLoanAdvanceUseCase) {
        this.manageLoanAdvanceUseCase = manageLoanAdvanceUseCase;
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:loan:read')")
    public Mono<ResponseEntity<ApiResponse<List<LoanAdvanceResponse>>>> listAll() {
        return manageLoanAdvanceUseCase.listAll()
                .map(LoanAdvanceResponse::from)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Loan advances fetched.")));
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:loan:create')")
    public Mono<ResponseEntity<ApiResponse<LoanAdvanceResponse>>> requestLoanAdvance(
            @Valid @RequestBody Mono<RequestLoanAdvanceRequest> requestMono) {
        return requestMono.map(RequestLoanAdvanceRequest::toCommand)
                .flatMap(manageLoanAdvanceUseCase::requestLoanAdvance)
                .map(LoanAdvanceResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Loan advance requested.")));
    }

    @PutMapping("/{loanAdvanceId}/approve")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:loan:approve')")
    public Mono<ResponseEntity<ApiResponse<LoanAdvanceResponse>>> approveLoanAdvance(
            @PathVariable UUID loanAdvanceId) {
        return manageLoanAdvanceUseCase.approveLoanAdvance(loanAdvanceId)
                .map(LoanAdvanceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Loan advance approved.")));
    }

    @PutMapping("/{loanAdvanceId}/reject")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:loan:approve')")
    public Mono<ResponseEntity<ApiResponse<LoanAdvanceResponse>>> rejectLoanAdvance(
            @PathVariable UUID loanAdvanceId,
            @RequestBody Mono<RejectLoanRequest> requestMono) {
        return requestMono.flatMap(req -> manageLoanAdvanceUseCase.rejectLoanAdvance(loanAdvanceId, req.motif()))
                .map(LoanAdvanceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Loan advance rejected.")));
    }

    @GetMapping("/{loanAdvanceId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:loan:read')")
    public Mono<ResponseEntity<ApiResponse<LoanAdvanceResponse>>> getLoanAdvance(
            @PathVariable UUID loanAdvanceId) {
        return manageLoanAdvanceUseCase.getLoanAdvance(loanAdvanceId)
                .map(LoanAdvanceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Loan advance fetched.")));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:loan:read')")
    public Mono<ResponseEntity<ApiResponse<List<LoanAdvanceResponse>>>> listByEmployee(
            @PathVariable UUID employeeId) {
        return manageLoanAdvanceUseCase.listByEmployee(employeeId)
                .map(LoanAdvanceResponse::from)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Loan advances fetched.")));
    }

    @GetMapping("/mine")
    public Mono<ResponseEntity<ApiResponse<List<LoanAdvanceResponse>>>> getMyLoans() {
        return manageLoanAdvanceUseCase.getMyLoans()
                .map(LoanAdvanceResponse::from)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "My loan advances fetched.")));
    }

    @PostMapping("/mine")
    public Mono<ResponseEntity<ApiResponse<LoanAdvanceResponse>>> requestMyLoan(
            @RequestBody Mono<MyLoanRequest> requestMono) {
        return requestMono.flatMap(req ->
                manageLoanAdvanceUseCase.requestMyLoan(req.montant(), req.nbEcheances(), req.motif()))
                .map(LoanAdvanceResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Loan advance requested.")));
    }

    public record MyLoanRequest(BigDecimal montant, int nbEcheances, String motif) {}

    // --- Request/Response DTOs ---

    public record RequestLoanAdvanceRequest(UUID employeeId, BigDecimal montant, int nbEcheances, String motif) {
        RequestLoanAdvanceCommand toCommand() {
            return new RequestLoanAdvanceCommand(employeeId, montant, nbEcheances, motif);
        }
    }

    public record RejectLoanRequest(String motif) {}

    public record LoanAdvanceResponse(UUID id, UUID employeeId, BigDecimal montant, BigDecimal soldeRestant,
            BigDecimal mensualite, String status, LocalDate dateDebut, int nbEcheances, String motif,
            UUID approvedBy) {
        static LoanAdvanceResponse from(LoanAdvance la) {
            return new LoanAdvanceResponse(la.id(), la.employeeId(), la.montant(), la.soldeRestant(),
                    la.mensualite(), la.status().name(), la.dateDebut(), la.nbEcheances(), la.motif(),
                    la.approvedBy());
        }
    }
}
