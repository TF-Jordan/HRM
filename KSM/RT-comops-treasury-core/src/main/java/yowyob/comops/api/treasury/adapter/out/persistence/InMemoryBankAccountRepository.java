package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.treasury.application.port.out.BankAccountRepository;
import yowyob.comops.api.treasury.domain.model.BankAccount;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryBankAccountRepository implements BankAccountRepository {

    private final Map<UUID, BankAccount> bankAccounts = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsByAccountNumber(UUID tenantId, UUID organizationId, String accountNumber) {
        return Mono.fromSupplier(() -> bankAccounts.values().stream()
                .filter(account -> account.tenantId().equals(tenantId))
                .filter(account -> account.organizationId().equals(organizationId))
                .anyMatch(account -> account.accountNumber().equalsIgnoreCase(accountNumber)));
    }

    @Override
    public Mono<BankAccount> findById(UUID bankAccountId) {
        return Mono.justOrEmpty(bankAccounts.get(bankAccountId));
    }

    @Override
    public Flux<BankAccount> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(bankAccounts.values().stream()
                .filter(account -> account.tenantId().equals(tenantId))
                .filter(account -> account.organizationId().equals(organizationId))
                .sorted((left, right) -> left.createdAt().compareTo(right.createdAt())));
    }

    @Override
    public Mono<BankAccount> findByOwnerThirdPartyId(UUID tenantId, UUID organizationId, UUID ownerThirdPartyId) {
        return Mono.justOrEmpty(bankAccounts.values().stream()
                .filter(account -> account.tenantId().equals(tenantId))
                .filter(account -> account.organizationId().equals(organizationId))
                .filter(account -> ownerThirdPartyId.equals(account.ownerThirdPartyId()))
                .findFirst());
    }

    @Override
    public Mono<BankAccount> save(BankAccount bankAccount) {
        return Mono.fromSupplier(() -> {
            bankAccounts.put(bankAccount.id(), bankAccount);
            return bankAccount;
        });
    }
}
