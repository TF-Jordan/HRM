package yowyob.comops.api.cashier.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CashDocumentRepository extends ReactiveCrudRepository<CashDocumentEntity, UUID> {
}
