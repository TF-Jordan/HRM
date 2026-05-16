package yowyob.comops.api.kernel.application.service;

import yowyob.comops.api.kernel.application.port.in.ListSystemAuditUseCase;
import yowyob.comops.api.kernel.application.port.in.RecordSystemAuditUseCase;
import yowyob.comops.api.kernel.application.port.out.SystemAuditRepository;
import yowyob.comops.api.kernel.domain.model.SystemAuditEntry;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SystemAuditService implements RecordSystemAuditUseCase, ListSystemAuditUseCase {

    private final SystemAuditRepository repository;

    public SystemAuditService(SystemAuditRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Void> record(java.util.UUID tenantId, java.util.UUID organizationId, java.util.UUID actorUserId,
            String action, String targetType, String targetId, String payloadSummary) {
        return repository.save(SystemAuditEntry.record(tenantId, organizationId, actorUserId, action, targetType,
                targetId, payloadSummary)).then();
    }

    @Override
    public Flux<SystemAuditEntry> listCurrentUserActivity(int limit) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> repository.findByTenantIdAndActorUserId(context.tenantId(), context.userId(),
                        sanitizeLimit(limit)));
    }

    @Override
    public Flux<SystemAuditEntry> listCurrentOrganizationActivity(int limit) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> repository.findByTenantIdAndOrganizationId(context.tenantId(),
                        context.organizationId(), sanitizeLimit(limit)));
    }

    private int sanitizeLimit(int limit) {
        if (limit <= 0) {
            return 50;
        }
        return Math.min(limit, 200);
    }
}
