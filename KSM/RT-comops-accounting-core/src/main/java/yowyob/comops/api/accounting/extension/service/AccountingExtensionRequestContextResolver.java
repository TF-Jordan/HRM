package yowyob.comops.api.accounting.extension.service;

import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AccountingExtensionRequestContextResolver {

    public Mono<AccountingExtensionRequestContext> resolve(ServerHttpRequest request) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .map(AccountingExtensionRequestContext::from);
    }
}
