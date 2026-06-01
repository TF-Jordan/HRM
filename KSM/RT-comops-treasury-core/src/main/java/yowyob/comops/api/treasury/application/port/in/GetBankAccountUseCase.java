package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.BankAccount;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface GetBankAccountUseCase {
    Mono<BankAccount> getBankAccount(UUID bankAccountId);
}
