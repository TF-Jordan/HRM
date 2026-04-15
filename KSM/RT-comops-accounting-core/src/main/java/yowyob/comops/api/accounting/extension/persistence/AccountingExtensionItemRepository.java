package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountingExtensionItemRepository
        extends ReactiveCrudRepository<AccountingExtensionItemEntity, String> {

    Flux<AccountingExtensionItemEntity> findAllByScopeAndItemType(String scope, String itemType);

    Mono<Void> deleteByScopeAndItemTypeAndItemId(String scope, String itemType, UUID itemId);

    Mono<Void> deleteByScopeAndItemType(String scope, String itemType);
}
