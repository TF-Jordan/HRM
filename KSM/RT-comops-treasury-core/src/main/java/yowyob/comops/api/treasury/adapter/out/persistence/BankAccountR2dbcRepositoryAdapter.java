package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.treasury.application.port.out.BankAccountRepository;
import yowyob.comops.api.treasury.domain.model.BankAccount;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class BankAccountR2dbcRepositoryAdapter implements BankAccountRepository {

    private final BankAccountSpringDataRepository repository;

    public BankAccountR2dbcRepositoryAdapter(BankAccountSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByAccountNumber(UUID tenantId, UUID organizationId, String accountNumber) {
        return repository.existsByTenantIdAndOrganizationIdAndAccountNumberIgnoreCase(tenantId, organizationId, accountNumber);
    }

    @Override
    public Mono<BankAccount> findById(UUID bankAccountId) {
        return repository.findById(bankAccountId).map(this::toDomain);
    }

    @Override
    public Flux<BankAccount> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .map(this::toDomain);
    }

    @Override
    public Mono<BankAccount> findByOwnerThirdPartyId(UUID tenantId, UUID organizationId, UUID ownerThirdPartyId) {
        return repository.findFirstByTenantIdAndOrganizationIdAndOwnerThirdPartyId(tenantId, organizationId, ownerThirdPartyId)
                .map(this::toDomain);
    }

    @Override
    public Mono<BankAccount> save(BankAccount bankAccount) {
        BankAccountEntity entity = new BankAccountEntity(bankAccount.id(), bankAccount.tenantId(), bankAccount.createdAt(),
                bankAccount.updatedAt(), bankAccount.organizationId(), bankAccount.bankThirdPartyId(),
                bankAccount.ownerThirdPartyId(), bankAccount.bankName(), bankAccount.accountNumber(), bankAccount.iban(),
                bankAccount.currency(), bankAccount.status());
        return repository.save(entity).map(this::toDomain);
    }

    private BankAccount toDomain(BankAccountEntity entity) {
        return BankAccount.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.bankThirdPartyId(), entity.ownerThirdPartyId(), entity.bankName(), entity.accountNumber(),
                entity.iban(), entity.currency(), entity.status());
    }
}
