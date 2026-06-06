package yowyob.comops.api.hrm.adapter.in.web;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.*;
import yowyob.comops.api.hrm.domain.model.ExpenseLine;
import yowyob.comops.api.hrm.domain.model.ExpenseReport;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/hrm/expenses")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class ExpenseController {

    private final ManageExpenseUseCase expenseUseCase;

    public ExpenseController(ManageExpenseUseCase expenseUseCase) {
        this.expenseUseCase = expenseUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:expense:create')")
    public Mono<ResponseEntity<ApiResponse<ExpenseReportResponse>>> createExpenseReport(
            @Valid @RequestBody Mono<CreateExpenseReportRequest> requestMono) {
        return requestMono.map(CreateExpenseReportRequest::toCommand)
                .flatMap(expenseUseCase::createExpenseReport)
                .map(ExpenseReportResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Expense report created.")));
    }

    @PostMapping("/{id}/lines")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:expense:create')")
    public Mono<ResponseEntity<ApiResponse<ExpenseLineResponse>>> addExpenseLine(
            @PathVariable UUID id, @Valid @RequestBody Mono<AddExpenseLineRequest> requestMono) {
        return requestMono.map(r -> r.toCommand(id))
                .flatMap(expenseUseCase::addExpenseLine)
                .map(ExpenseLineResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Expense line added.")));
    }

    @PutMapping("/{id}/submit")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:expense:create')")
    public Mono<ResponseEntity<ApiResponse<ExpenseReportResponse>>> submitExpenseReport(@PathVariable UUID id) {
        return expenseUseCase.submitExpenseReport(id).map(ExpenseReportResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Expense report submitted.")));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:expense:manage')")
    public Mono<ResponseEntity<ApiResponse<ExpenseReportResponse>>> approveExpenseReport(@PathVariable UUID id) {
        return expenseUseCase.approveExpenseReport(id).map(ExpenseReportResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Expense report approved.")));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:expense:manage')")
    public Mono<ResponseEntity<ApiResponse<ExpenseReportResponse>>> rejectExpenseReport(@PathVariable UUID id) {
        return expenseUseCase.rejectExpenseReport(id).map(ExpenseReportResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Expense report rejected.")));
    }

    @PutMapping("/{id}/reimburse")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:expense:manage')")
    public Mono<ResponseEntity<ApiResponse<ExpenseReportResponse>>> reimburseExpenseReport(@PathVariable UUID id) {
        return expenseUseCase.reimburseExpenseReport(id).map(ExpenseReportResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Expense report reimbursed.")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:expense:read')")
    public Mono<ResponseEntity<ApiResponse<ExpenseReportResponse>>> getExpenseReport(@PathVariable UUID id) {
        return expenseUseCase.getExpenseReport(id).map(ExpenseReportResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Expense report fetched.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:expense:read')")
    public Mono<ResponseEntity<ApiResponse<List<ExpenseReportResponse>>>> listExpenseReports(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) UUID missionOrderId,
            @RequestParam(required = false) String status) {
        var flux = missionOrderId != null
                ? expenseUseCase.listExpenseReportsByMission(missionOrderId)
                : employeeId != null
                ? expenseUseCase.listExpenseReportsByEmployee(employeeId)
                : expenseUseCase.listExpenseReports(organizationId, status);
        return flux.map(ExpenseReportResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Expense reports fetched.")));
    }

    @GetMapping("/{id}/lines")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:expense:read')")
    public Mono<ResponseEntity<ApiResponse<List<ExpenseLineResponse>>>> listExpenseLines(@PathVariable UUID id) {
        return expenseUseCase.listExpenseLinesByReport(id)
                .map(ExpenseLineResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Expense lines fetched.")));
    }

    public record CreateExpenseReportRequest(UUID employeeId, String periode, String motif,
            UUID missionOrderId) {
        CreateExpenseReportCommand toCommand() {
            return new CreateExpenseReportCommand(employeeId, periode, motif, missionOrderId);
        }
    }

    public record AddExpenseLineRequest(String description, BigDecimal montant, String categorie,
            UUID justificatifFileId) {
        AddExpenseLineCommand toCommand(UUID expenseReportId) {
            return new AddExpenseLineCommand(expenseReportId, description, montant, categorie, justificatifFileId);
        }
    }

    public record ExpenseReportResponse(UUID id, UUID employeeId, String periode,
            BigDecimal totalMontant, String motif, String status, UUID missionOrderId) {
        static ExpenseReportResponse from(ExpenseReport r) {
            return new ExpenseReportResponse(r.id(), r.employeeId(), r.periode(),
                    r.totalMontant(), r.motif(), r.status().name(), r.missionOrderId());
        }
    }

    public record ExpenseLineResponse(UUID id, UUID expenseReportId, String description,
            BigDecimal montant, String categorie, UUID justificatifFileId) {
        static ExpenseLineResponse from(ExpenseLine l) {
            return new ExpenseLineResponse(l.id(), l.expenseReportId(), l.description(),
                    l.montant(), l.categorie(), l.justificatifFileId());
        }
    }
}
