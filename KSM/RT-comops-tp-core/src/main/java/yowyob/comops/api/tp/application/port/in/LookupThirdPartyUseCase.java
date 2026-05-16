package yowyob.comops.api.tp.application.port.in;

import yowyob.comops.api.tp.domain.model.ThirdParty;
import reactor.core.publisher.Mono;

public interface LookupThirdPartyUseCase {
    Mono<ThirdParty> findByBankAccountNumber(java.util.UUID organizationId, String bankAccountNumber, String role,
            Boolean prospect);
    Mono<ThirdParty> findByAccountingAccount(java.util.UUID organizationId, String accountingAccount, String role,
            Boolean prospect);
}
