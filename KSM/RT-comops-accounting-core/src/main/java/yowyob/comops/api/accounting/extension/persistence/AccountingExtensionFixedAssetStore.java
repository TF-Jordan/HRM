package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingOperationsService;

@Component
public class AccountingExtensionFixedAssetStore {

    private final AccountingExtensionFixedAssetRepository repository;

    public AccountingExtensionFixedAssetStore(AccountingExtensionFixedAssetRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingOperationsService.FixedAsset> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingOperationsService.FixedAsset asset) {
        return repository.save(toEntity(asset)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionFixedAssetEntity toEntity(AccountingOperationsService.FixedAsset asset) {
        return new AccountingExtensionFixedAssetEntity(
                asset.id(),
                asset.organizationId(),
                asset.reference(),
                asset.label(),
                asset.acquisitionCost(),
                asset.usefulLifeMonths(),
                asset.accumulatedDepreciation(),
                asset.status(),
                asset.acquiredAt(),
                asset.lastDepreciatedAt());
    }

    private AccountingOperationsService.FixedAsset toDomain(AccountingExtensionFixedAssetEntity entity) {
        return new AccountingOperationsService.FixedAsset(
                entity.id(),
                entity.organizationId(),
                entity.reference(),
                entity.label(),
                entity.acquisitionCost(),
                entity.usefulLifeMonths(),
                entity.accumulatedDepreciation(),
                entity.status(),
                entity.acquiredAt(),
                entity.lastDepreciatedAt());
    }
}
