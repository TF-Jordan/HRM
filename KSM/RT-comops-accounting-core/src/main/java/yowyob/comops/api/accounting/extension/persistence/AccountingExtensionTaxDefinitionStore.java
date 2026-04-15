package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionTaxDefinitionStore {

    private final AccountingExtensionTaxDefinitionRepository repository;

    public AccountingExtensionTaxDefinitionStore(AccountingExtensionTaxDefinitionRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.TaxDefinition> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.TaxDefinition taxDefinition) {
        return repository.save(toEntity(taxDefinition)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionTaxDefinitionEntity toEntity(AccountingBookkeepingService.TaxDefinition taxDefinition) {
        return new AccountingExtensionTaxDefinitionEntity(
                taxDefinition.id(),
                taxDefinition.organizationId(),
                taxDefinition.code(),
                taxDefinition.label(),
                taxDefinition.rate(),
                taxDefinition.active(),
                taxDefinition.createdAt());
    }

    private AccountingBookkeepingService.TaxDefinition toDomain(AccountingExtensionTaxDefinitionEntity entity) {
        return new AccountingBookkeepingService.TaxDefinition(
                entity.id(),
                entity.organizationId(),
                entity.code(),
                entity.label(),
                entity.rate(),
                entity.active(),
                entity.createdAt());
    }
}
