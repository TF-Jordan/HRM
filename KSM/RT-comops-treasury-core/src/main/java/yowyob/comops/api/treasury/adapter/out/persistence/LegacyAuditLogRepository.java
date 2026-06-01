package yowyob.comops.api.treasury.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LegacyAuditLogRepository extends ReactiveCrudRepository<LegacyAuditLogEntity, UUID> {
}
