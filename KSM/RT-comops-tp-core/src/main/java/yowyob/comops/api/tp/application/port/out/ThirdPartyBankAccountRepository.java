package yowyob.comops.api.tp.application.port.out;

import yowyob.comops.api.tp.domain.model.ThirdPartyBankAccount;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ThirdPartyBankAccountRepository {

    Mono<Boolean> existsByIban(UUID tenantId, UUID thirdPartyId, String iban);

    Mono<ThirdPartyBankAccount> findById(UUID tenantId, UUID bankAccountId);

    Flux<ThirdPartyBankAccount> findByThirdPartyId(UUID tenantId, UUID thirdPartyId);

    Mono<ThirdPartyBankAccount> findPrimaryByThirdPartyId(UUID tenantId, UUID thirdPartyId);

    Mono<ThirdPartyBankAccount> findByIban(UUID tenantId, String iban);

    Mono<ThirdPartyBankAccount> save(ThirdPartyBankAccount bankAccount);

    Mono<Void> deleteById(UUID tenantId, UUID bankAccountId);
}
