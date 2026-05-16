package yowyob.comops.api.treasury.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.treasury.application.port.in.GetBankStatementUseCase;
import yowyob.comops.api.treasury.application.port.in.ListBankStatementsUseCase;
import yowyob.comops.api.treasury.application.port.in.RegisterBankStatementCommand;
import yowyob.comops.api.treasury.application.port.in.RegisterBankStatementUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping({"/api/treasury/statements", "/api/banking/statements"})
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
public class BankStatementController {

    private final RegisterBankStatementUseCase registerBankStatementUseCase;
    private final GetBankStatementUseCase getBankStatementUseCase;
    private final ListBankStatementsUseCase listBankStatementsUseCase;

    public BankStatementController(RegisterBankStatementUseCase registerBankStatementUseCase,
            GetBankStatementUseCase getBankStatementUseCase,
            ListBankStatementsUseCase listBankStatementsUseCase) {
        this.registerBankStatementUseCase = registerBankStatementUseCase;
        this.getBankStatementUseCase = getBankStatementUseCase;
        this.listBankStatementsUseCase = listBankStatementsUseCase;
    }

    @PostMapping({"", "/import"})
    public Mono<ResponseEntity<ApiResponse<BankStatementResponse>>> register(
            @Valid @RequestBody Mono<RegisterBankStatementRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> registerBankStatementUseCase.registerStatement(new RegisterBankStatementCommand(
                        tuple.getT2().tenantId(), tuple.getT1().organizationId(), tuple.getT1().bankAccountId(),
                        tuple.getT1().statementNumber(), tuple.getT1().statementDate(), tuple.getT1().openingBalance(),
                        tuple.getT1().closingBalance())))
                .map(BankStatementResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Bank statement registered.")));
    }

    @GetMapping("/{statementId}")
    public Mono<ResponseEntity<ApiResponse<BankStatementResponse>>> getStatement(
            @PathVariable("statementId") UUID statementId) {
        return getBankStatementUseCase.getStatement(statementId)
                .map(BankStatementResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Bank statement fetched.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<BankStatementResponse>>>> listStatements(
            @RequestParam("organizationId") UUID organizationId,
            @RequestParam(name = "bankAccountId", required = false) UUID bankAccountId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listBankStatementsUseCase.listStatements(context.tenantId(), organizationId,
                        bankAccountId)
                        .map(BankStatementResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Bank statements fetched.")));
    }
}
