package yowyob.comops.api.administration.application.port.out;

import yowyob.comops.api.administration.domain.model.AdminAuditEntry;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdminAuditRepository {
    Mono<AdminAuditEntry> save(AdminAuditEntry entry);

    Flux<AdminAuditEntry> findByTenantId(UUID tenantId, int limit);
}
