package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface AccountingExtensionCashRegisterPostingRepository
        extends ReactiveCrudRepository<AccountingExtensionCashRegisterPostingEntity, UUID> {
}
