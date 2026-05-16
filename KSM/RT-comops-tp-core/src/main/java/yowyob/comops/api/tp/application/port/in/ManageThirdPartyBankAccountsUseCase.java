package yowyob.comops.api.tp.application.port.in;

import yowyob.comops.api.tp.domain.model.ThirdParty;
import yowyob.comops.api.tp.domain.model.ThirdPartyBankAccount;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageThirdPartyBankAccountsUseCase {
    Flux<ThirdPartyBankAccount> listBankAccounts(UUID thirdPartyId);
    Mono<ThirdPartyBankAccount> addBankAccount(AddThirdPartyBankAccountCommand command);
    Mono<Void> removeBankAccount(UUID thirdPartyId, UUID bankAccountId);
    Mono<Void> setPrimaryBankAccount(UUID thirdPartyId, UUID bankAccountId);
    Mono<ThirdParty> definePrimaryBankAccount(UUID thirdPartyId, String iban);
}
