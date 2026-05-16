package yowyob.comops.api.hrm.adapter.in.web;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.CreateTrainingBudgetCommand;
import yowyob.comops.api.hrm.application.port.in.ManageTrainingBudgetUseCase;
import yowyob.comops.api.hrm.domain.model.TrainingBudget;

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
@RequestMapping("/api/v1/hrm/training-budgets")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class TrainingBudgetController {

    private final ManageTrainingBudgetUseCase budgetUseCase;

    public TrainingBudgetController(ManageTrainingBudgetUseCase budgetUseCase) {
        this.budgetUseCase = budgetUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:budget:create')")
    public Mono<ResponseEntity<ApiResponse<TrainingBudgetResponse>>> createBudget(
            @Valid @RequestBody Mono<CreateTrainingBudgetRequest> requestMono) {
        return requestMono.map(CreateTrainingBudgetRequest::toCommand)
                .flatMap(budgetUseCase::createBudget)
                .map(TrainingBudgetResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Training budget created.")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:budget:read')")
    public Mono<ResponseEntity<ApiResponse<TrainingBudgetResponse>>> getBudget(@PathVariable UUID id) {
        return budgetUseCase.getBudget(id).map(TrainingBudgetResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Training budget fetched.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:budget:read')")
    public Mono<ResponseEntity<ApiResponse<List<TrainingBudgetResponse>>>> listBudgets(
            @RequestParam UUID organizationId, @RequestParam int annee) {
        return budgetUseCase.listBudgets(organizationId, annee)
                .map(TrainingBudgetResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Training budgets fetched.")));
    }

    @PutMapping("/{id}/engage")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:budget:manage')")
    public Mono<ResponseEntity<ApiResponse<TrainingBudgetResponse>>> engageBudget(
            @PathVariable UUID id, @Valid @RequestBody Mono<MontantRequest> requestMono) {
        return requestMono.flatMap(req -> budgetUseCase.engageBudget(id, req.montant()))
                .map(TrainingBudgetResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Budget engaged.")));
    }

    @PutMapping("/{id}/realiser")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:budget:manage')")
    public Mono<ResponseEntity<ApiResponse<TrainingBudgetResponse>>> realiseBudget(
            @PathVariable UUID id, @Valid @RequestBody Mono<MontantRequest> requestMono) {
        return requestMono.flatMap(req -> budgetUseCase.realiseBudget(id, req.montant()))
                .map(TrainingBudgetResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Budget realized.")));
    }

    public record CreateTrainingBudgetRequest(UUID organizationId, UUID agencyId, int annee,
            BigDecimal montantAlloue) {
        CreateTrainingBudgetCommand toCommand() {
            return new CreateTrainingBudgetCommand(organizationId, agencyId, annee, montantAlloue);
        }
    }

    public record MontantRequest(BigDecimal montant) {
    }

    public record TrainingBudgetResponse(UUID id, UUID organizationId, UUID agencyId, int annee,
            BigDecimal montantAlloue, BigDecimal montantEngage, BigDecimal montantRealise) {
        static TrainingBudgetResponse from(TrainingBudget b) {
            return new TrainingBudgetResponse(b.id(), b.organizationId(), b.agencyId(), b.annee(),
                    b.montantAlloue(), b.montantEngage(), b.montantRealise());
        }
    }
}
