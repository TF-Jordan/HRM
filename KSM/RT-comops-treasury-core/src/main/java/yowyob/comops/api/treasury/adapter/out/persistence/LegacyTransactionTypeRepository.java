package yowyob.comops.api.treasury.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LegacyTransactionTypeRepository extends ReactiveCrudRepository<LegacyTransactionTypeEntity, UUID> {
}
