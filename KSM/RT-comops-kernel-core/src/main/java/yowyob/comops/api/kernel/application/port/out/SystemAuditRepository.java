package yowyob.comops.api.kernel.application.port.out;

import yowyob.comops.api.kernel.domain.model.SystemAuditEntry;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SystemAuditRepository {
    Mono<SystemAuditEntry> save(SystemAuditEntry entry);
    Flux<SystemAuditEntry> findByTenantIdAndActorUserId(UUID tenantId, UUID actorUserId, int limit);
    Flux<SystemAuditEntry> findByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId, int limit);
}
