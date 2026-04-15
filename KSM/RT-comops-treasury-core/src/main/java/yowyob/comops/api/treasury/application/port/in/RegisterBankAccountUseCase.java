package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.BankAccount;
import reactor.core.publisher.Mono;

public interface RegisterBankAccountUseCase {

    Mono<BankAccount> register(RegisterBankAccountCommand command);
}
