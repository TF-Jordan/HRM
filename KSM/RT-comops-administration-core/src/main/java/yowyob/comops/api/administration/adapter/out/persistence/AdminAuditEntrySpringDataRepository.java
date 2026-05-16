package yowyob.comops.api.administration.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AdminAuditEntrySpringDataRepository extends ReactiveCrudRepository<AdminAuditEntryEntity, UUID> {
    Flux<AdminAuditEntryEntity> findTop200ByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
