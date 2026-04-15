package yowyob.comops.api.kernel.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface SystemAuditEntrySpringDataRepository extends ReactiveCrudRepository<SystemAuditEntryEntity, UUID> {
    Flux<SystemAuditEntryEntity> findTop200ByTenantIdAndActorUserIdOrderByCreatedAtDesc(UUID tenantId, UUID actorUserId);
    Flux<SystemAuditEntryEntity> findTop200ByTenantIdAndOrganizationIdOrderByCreatedAtDesc(UUID tenantId, UUID organizationId);
}
