package yowyob.comops.api.cashier.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CashRegisterRepository extends ReactiveCrudRepository<CashRegisterEntity, UUID> {
}
