package yowyob.comops.api.kernel.application.service;

import yowyob.comops.api.kernel.domain.MissingTenantException;
import yowyob.comops.api.kernel.domain.model.TenantContext;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

public final class ReactiveRequestContextHolder {

    private ReactiveRequestContextHolder() {
    }

    public static Context withTenantContext(Context context, TenantContext tenantContext) {
        return context.put(RequestContextKeys.TENANT_CONTEXT, tenantContext);
    }

    public static Mono<TenantContext> getRequiredContext() {
        return Mono.deferContextual(ReactiveRequestContextHolder::getRequiredContext);
    }

    public static Mono<TenantContext> getRequiredContext(ContextView contextView) {
        if (!contextView.hasKey(RequestContextKeys.TENANT_CONTEXT)) {
            return Mono.error(new MissingTenantException());
        }
        return Mono.just(contextView.get(RequestContextKeys.TENANT_CONTEXT));
    }
}
