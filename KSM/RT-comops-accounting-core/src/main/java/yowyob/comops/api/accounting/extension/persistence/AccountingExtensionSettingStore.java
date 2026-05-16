package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionSettingStore {

    private final AccountingExtensionSettingRepository repository;

    public AccountingExtensionSettingStore(AccountingExtensionSettingRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.AccountingSetting> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.AccountingSetting setting) {
        return repository.save(toEntity(setting)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionSettingEntity toEntity(AccountingBookkeepingService.AccountingSetting setting) {
        return new AccountingExtensionSettingEntity(
                setting.id(),
                setting.organizationId(),
                setting.code(),
                setting.value(),
                setting.updatedAt());
    }

    private AccountingBookkeepingService.AccountingSetting toDomain(AccountingExtensionSettingEntity entity) {
        return new AccountingBookkeepingService.AccountingSetting(
                entity.id(),
                entity.organizationId(),
                entity.code(),
                entity.value(),
                entity.updatedAt());
    }
}
