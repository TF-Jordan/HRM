package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AccountingExtensionFixedAssetRepository
        extends ReactiveCrudRepository<AccountingExtensionFixedAssetEntity, UUID> {
    Flux<AccountingExtensionFixedAssetEntity> findAllByOrganizationId(UUID organizationId);
}
