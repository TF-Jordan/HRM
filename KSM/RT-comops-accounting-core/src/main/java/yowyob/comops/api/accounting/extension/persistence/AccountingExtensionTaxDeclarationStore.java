package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingOperationsService;

@Component
public class AccountingExtensionTaxDeclarationStore {

    private final AccountingExtensionTaxDeclarationRepository repository;

    public AccountingExtensionTaxDeclarationStore(AccountingExtensionTaxDeclarationRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingOperationsService.TaxDeclaration> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingOperationsService.TaxDeclaration declaration) {
        return repository.save(toEntity(declaration)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionTaxDeclarationEntity toEntity(AccountingOperationsService.TaxDeclaration declaration) {
        return new AccountingExtensionTaxDeclarationEntity(
                declaration.id(),
                declaration.organizationId(),
                declaration.taxType(),
                declaration.periodLabel(),
                declaration.taxableBase(),
                declaration.taxAmount(),
                declaration.status(),
                declaration.createdAt(),
                declaration.submittedAt());
    }

    private AccountingOperationsService.TaxDeclaration toDomain(AccountingExtensionTaxDeclarationEntity entity) {
        return new AccountingOperationsService.TaxDeclaration(
                entity.id(),
                entity.organizationId(),
                entity.taxType(),
                entity.periodLabel(),
                entity.taxableBase(),
                entity.taxAmount(),
                entity.status(),
                entity.createdAt(),
                entity.submittedAt());
    }
}
