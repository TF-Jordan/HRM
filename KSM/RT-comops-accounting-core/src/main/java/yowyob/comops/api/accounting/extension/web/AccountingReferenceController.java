package yowyob.comops.api.accounting.extension.web;

import yowyob.comops.api.accounting.extension.service.AccountingKernelFacade;
import yowyob.comops.api.accounting.extension.web.AccountingReferenceDataView;
import yowyob.comops.api.accounting.extension.web.OrganizationSummaryView;
import yowyob.comops.api.accounting.extension.service.AccountingExtensionRequestContextResolver;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/accounting-service/reference-data")
public class AccountingReferenceController {

    private final AccountingKernelFacade accountingKernelFacade;
    private final AccountingExtensionRequestContextResolver contextResolver;

    public AccountingReferenceController(AccountingKernelFacade accountingKernelFacade,
            AccountingExtensionRequestContextResolver contextResolver) {
        this.accountingKernelFacade = accountingKernelFacade;
        this.contextResolver = contextResolver;
    }

    @GetMapping("/organizations/my")
    public Mono<java.util.List<OrganizationSummaryView>> listMyOrganizations(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMap(accountingKernelFacade::listMyOrganizations);
    }

    @GetMapping("/reporting")
    public Mono<AccountingReferenceDataView> loadReportingReferenceData(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMap(accountingKernelFacade::loadReportingReferenceData);
    }
}
