package yowyob.comops.api.treasury.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LegacyStatementLineRepository extends ReactiveCrudRepository<LegacyStatementLineEntity, UUID> {
}
