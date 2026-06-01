package yowyob.comops.api.accounting.adapter.in.web;

import yowyob.comops.api.accounting.application.port.in.ListAccountingJournalsUseCase;
import yowyob.comops.api.accounting.application.port.in.ListOpenPayablesUseCase;
import yowyob.comops.api.accounting.application.port.in.ListOpenReceivablesUseCase;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/accounting")
@PreAuthorize("@businessAccessPolicy.canReadAccounting(authentication)")
public class AccountingReadController {

    private final ListAccountingJournalsUseCase listAccountingJournalsUseCase;
    private final ListOpenReceivablesUseCase listOpenReceivablesUseCase;
    private final ListOpenPayablesUseCase listOpenPayablesUseCase;

    public AccountingReadController(ListAccountingJournalsUseCase listAccountingJournalsUseCase,
            ListOpenReceivablesUseCase listOpenReceivablesUseCase,
            ListOpenPayablesUseCase listOpenPayablesUseCase) {
        this.listAccountingJournalsUseCase = listAccountingJournalsUseCase;
        this.listOpenReceivablesUseCase = listOpenReceivablesUseCase;
        this.listOpenPayablesUseCase = listOpenPayablesUseCase;
    }

    @GetMapping("/journals")
    public Mono<ResponseEntity<ApiResponse<List<AccountingJournalResponse>>>> listJournals(
            @RequestParam("organizationId") UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> listAccountingJournalsUseCase.listJournals(context.tenantId(), organizationId))
                .map(AccountingJournalResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Accounting journals fetched.")));
    }

    @GetMapping("/open-items/receivables")
    public Mono<ResponseEntity<ApiResponse<List<OpenAccountingItemResponse>>>> listOpenReceivables(
            @RequestParam("organizationId") UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> listOpenReceivablesUseCase.listOpenReceivables(context.tenantId(), organizationId))
                .map(OpenAccountingItemResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Open receivables fetched.")));
    }

    @GetMapping("/open-items/payables")
    public Mono<ResponseEntity<ApiResponse<List<OpenAccountingItemResponse>>>> listOpenPayables(
            @RequestParam("organizationId") UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> listOpenPayablesUseCase.listOpenPayables(context.tenantId(), organizationId))
                .map(OpenAccountingItemResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Open payables fetched.")));
    }
}
