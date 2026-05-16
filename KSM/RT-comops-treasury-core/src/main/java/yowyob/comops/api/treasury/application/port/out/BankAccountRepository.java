package yowyob.comops.api.treasury.application.port.out;

import yowyob.comops.api.treasury.domain.model.BankAccount;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankAccountRepository {

    Mono<Boolean> existsByAccountNumber(UUID tenantId, UUID organizationId, String accountNumber);

    Mono<BankAccount> findById(UUID bankAccountId);

    Flux<BankAccount> findByOrganizationId(UUID tenantId, UUID organizationId);

    Mono<BankAccount> findByOwnerThirdPartyId(UUID tenantId, UUID organizationId, UUID ownerThirdPartyId);

    Mono<BankAccount> save(BankAccount bankAccount);
}
