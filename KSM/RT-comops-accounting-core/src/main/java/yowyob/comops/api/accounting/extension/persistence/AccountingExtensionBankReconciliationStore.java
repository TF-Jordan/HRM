package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionBankReconciliationStore {

    private final AccountingExtensionBankReconciliationRepository repository;

    public AccountingExtensionBankReconciliationStore(AccountingExtensionBankReconciliationRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.BankReconciliation> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.BankReconciliation reconciliation) {
        return repository.save(toEntity(reconciliation)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionBankReconciliationEntity toEntity(
            AccountingBookkeepingService.BankReconciliation reconciliation) {
        return new AccountingExtensionBankReconciliationEntity(
                reconciliation.id(),
                reconciliation.organizationId(),
                reconciliation.reconciliationReference(),
                reconciliation.bankAccountNumber(),
                reconciliation.matchedAmount(),
                reconciliation.createdAt());
    }

    private AccountingBookkeepingService.BankReconciliation toDomain(
            AccountingExtensionBankReconciliationEntity entity) {
        return new AccountingBookkeepingService.BankReconciliation(
                entity.id(),
                entity.organizationId(),
                entity.reconciliationReference(),
                entity.bankAccountNumber(),
                entity.matchedAmount(),
                entity.createdAt());
    }
}
