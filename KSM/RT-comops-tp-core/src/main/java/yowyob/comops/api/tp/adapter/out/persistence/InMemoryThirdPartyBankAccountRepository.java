package yowyob.comops.api.tp.adapter.out.persistence;

import yowyob.comops.api.tp.application.port.out.ThirdPartyBankAccountRepository;
import yowyob.comops.api.tp.domain.model.ThirdPartyBankAccount;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryThirdPartyBankAccountRepository implements ThirdPartyBankAccountRepository {

    private final Map<UUID, ThirdPartyBankAccount> bankAccounts = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsByIban(UUID tenantId, UUID thirdPartyId, String iban) {
        return Mono.fromSupplier(() -> bankAccounts.values().stream()
                .filter(account -> account.tenantId().equals(tenantId))
                .filter(account -> account.thirdPartyId().equals(thirdPartyId))
                .anyMatch(account -> account.iban().equalsIgnoreCase(iban)));
    }

    @Override
    public Mono<ThirdPartyBankAccount> findById(UUID tenantId, UUID bankAccountId) {
        return Mono.justOrEmpty(bankAccounts.get(bankAccountId)).filter(account -> account.tenantId().equals(tenantId));
    }

    @Override
    public Flux<ThirdPartyBankAccount> findByThirdPartyId(UUID tenantId, UUID thirdPartyId) {
        return Flux.fromStream(bankAccounts.values().stream()
                .filter(account -> account.tenantId().equals(tenantId))
                .filter(account -> account.thirdPartyId().equals(thirdPartyId)));
    }

    @Override
    public Mono<ThirdPartyBankAccount> findPrimaryByThirdPartyId(UUID tenantId, UUID thirdPartyId) {
        return findByThirdPartyId(tenantId, thirdPartyId).filter(ThirdPartyBankAccount::primary).next();
    }

    @Override
    public Mono<ThirdPartyBankAccount> findByIban(UUID tenantId, String iban) {
        return Flux.fromStream(bankAccounts.values().stream()
                .filter(account -> account.tenantId().equals(tenantId))
                .filter(account -> account.iban().equalsIgnoreCase(iban)))
                .next();
    }

    @Override
    public Mono<ThirdPartyBankAccount> save(ThirdPartyBankAccount bankAccount) {
        return Mono.fromSupplier(() -> {
            bankAccounts.put(bankAccount.id(), bankAccount);
            return bankAccount;
        });
    }

    @Override
    public Mono<Void> deleteById(UUID tenantId, UUID bankAccountId) {
        return Mono.fromRunnable(() -> {
            ThirdPartyBankAccount existing = bankAccounts.get(bankAccountId);
            if (existing != null && existing.tenantId().equals(tenantId)) {
                bankAccounts.remove(bankAccountId);
            }
        });
    }
}
