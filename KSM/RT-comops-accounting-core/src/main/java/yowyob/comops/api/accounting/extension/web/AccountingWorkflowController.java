package yowyob.comops.api.accounting.extension.web;

import yowyob.comops.api.accounting.extension.service.AccountingClosingWorkflowService;
import yowyob.comops.api.accounting.extension.web.AccountingClosingPreviewView;
import yowyob.comops.api.accounting.extension.service.AccountingExtensionRequestContextResolver;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/accounting-service/workflows")
public class AccountingWorkflowController {

    private final AccountingClosingWorkflowService accountingClosingWorkflowService;
    private final AccountingExtensionRequestContextResolver contextResolver;

    public AccountingWorkflowController(AccountingClosingWorkflowService accountingClosingWorkflowService,
            AccountingExtensionRequestContextResolver contextResolver) {
        this.accountingClosingWorkflowService = accountingClosingWorkflowService;
        this.contextResolver = contextResolver;
    }

    @GetMapping("/closing/preview")
    public Mono<AccountingClosingPreviewView> previewClosing(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMap(accountingClosingWorkflowService::preview);
    }
}
