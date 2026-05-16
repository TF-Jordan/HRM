package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionStockMovementPostingStore {

    private final AccountingExtensionStockMovementPostingRepository repository;

    public AccountingExtensionStockMovementPostingStore(AccountingExtensionStockMovementPostingRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.StockMovementPosting> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.StockMovementPosting posting) {
        return repository.save(toEntity(posting)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionStockMovementPostingEntity toEntity(
            AccountingBookkeepingService.StockMovementPosting posting) {
        return new AccountingExtensionStockMovementPostingEntity(
                posting.id(),
                posting.organizationId(),
                posting.movementReference(),
                posting.movementType(),
                posting.valuationAmount(),
                posting.currency(),
                posting.createdAt());
    }

    private AccountingBookkeepingService.StockMovementPosting toDomain(
            AccountingExtensionStockMovementPostingEntity entity) {
        return new AccountingBookkeepingService.StockMovementPosting(
                entity.id(),
                entity.organizationId(),
                entity.movementReference(),
                entity.movementType(),
                entity.valuationAmount(),
                entity.currency(),
                entity.createdAt());
    }
}
