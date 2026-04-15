package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface AccountingExtensionBankReconciliationRepository
        extends ReactiveCrudRepository<AccountingExtensionBankReconciliationEntity, UUID> {
}
