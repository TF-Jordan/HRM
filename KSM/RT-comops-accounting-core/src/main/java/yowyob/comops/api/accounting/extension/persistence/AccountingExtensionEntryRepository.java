package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountingExtensionEntryRepository extends ReactiveCrudRepository<AccountingExtensionEntryEntity, UUID> {
    Flux<AccountingExtensionEntryEntity> findAllByOrderByCreatedAtDesc();

    Flux<AccountingExtensionEntryEntity> findAllByOrganizationId(UUID organizationId);

    Mono<Void> deleteByOrganizationId(UUID organizationId);
}
