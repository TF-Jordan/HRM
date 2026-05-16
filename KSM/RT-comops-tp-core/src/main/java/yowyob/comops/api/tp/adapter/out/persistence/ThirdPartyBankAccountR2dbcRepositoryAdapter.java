package yowyob.comops.api.tp.adapter.out.persistence;

import yowyob.comops.api.tp.application.port.out.ThirdPartyBankAccountRepository;
import yowyob.comops.api.tp.domain.model.ThirdPartyBankAccount;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ThirdPartyBankAccountR2dbcRepositoryAdapter implements ThirdPartyBankAccountRepository {

    private final ThirdPartyBankAccountSpringDataRepository repository;

    public ThirdPartyBankAccountR2dbcRepositoryAdapter(ThirdPartyBankAccountSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByIban(UUID tenantId, UUID thirdPartyId, String iban) {
        return repository.existsByTenantIdAndThirdPartyIdAndIbanIgnoreCase(tenantId, thirdPartyId, iban);
    }

    @Override
    public Mono<ThirdPartyBankAccount> findById(UUID tenantId, UUID bankAccountId) {
        return repository.findByIdAndTenantId(bankAccountId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<ThirdPartyBankAccount> findByThirdPartyId(UUID tenantId, UUID thirdPartyId) {
        return repository.findAllByTenantIdAndThirdPartyId(tenantId, thirdPartyId).map(this::toDomain);
    }

    @Override
    public Mono<ThirdPartyBankAccount> findPrimaryByThirdPartyId(UUID tenantId, UUID thirdPartyId) {
        return repository.findFirstByTenantIdAndThirdPartyIdAndPrimaryAccountTrue(tenantId, thirdPartyId)
                .map(this::toDomain);
    }

    @Override
    public Mono<ThirdPartyBankAccount> findByIban(UUID tenantId, String iban) {
        return repository.findFirstByTenantIdAndIbanIgnoreCase(tenantId, iban).map(this::toDomain);
    }

    @Override
    public Mono<ThirdPartyBankAccount> save(ThirdPartyBankAccount bankAccount) {
        return repository.save(toEntity(bankAccount)).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID tenantId, UUID bankAccountId) {
        return repository.findByIdAndTenantId(bankAccountId, tenantId).flatMap(repository::delete).then();
    }

    private ThirdPartyBankAccountEntity toEntity(ThirdPartyBankAccount bankAccount) {
        return new ThirdPartyBankAccountEntity(bankAccount.id(), bankAccount.tenantId(), bankAccount.createdAt(),
                bankAccount.updatedAt(), bankAccount.thirdPartyId(), bankAccount.label(), bankAccount.bankName(),
                bankAccount.iban(), bankAccount.swiftBic(), bankAccount.currency(), bankAccount.primary());
    }

    private ThirdPartyBankAccount toDomain(ThirdPartyBankAccountEntity entity) {
        return ThirdPartyBankAccount.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.thirdPartyId(), entity.label(), entity.bankName(), entity.iban(), entity.swiftBic(),
                entity.currency(), entity.primaryAccount());
    }
}
