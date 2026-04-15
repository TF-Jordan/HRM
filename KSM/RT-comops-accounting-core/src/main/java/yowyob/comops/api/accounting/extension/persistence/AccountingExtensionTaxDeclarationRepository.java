package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AccountingExtensionTaxDeclarationRepository
        extends ReactiveCrudRepository<AccountingExtensionTaxDeclarationEntity, UUID> {
    Flux<AccountingExtensionTaxDeclarationEntity> findAllByOrganizationId(UUID organizationId);
}
