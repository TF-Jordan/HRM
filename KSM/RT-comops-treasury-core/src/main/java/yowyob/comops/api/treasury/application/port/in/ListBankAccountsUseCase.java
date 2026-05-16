package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.BankAccount;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListBankAccountsUseCase {
    Flux<BankAccount> listBankAccounts(UUID tenantId, UUID organizationId);
}
