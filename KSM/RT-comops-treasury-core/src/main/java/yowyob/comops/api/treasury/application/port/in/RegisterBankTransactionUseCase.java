package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.BankTransaction;
import reactor.core.publisher.Mono;

public interface RegisterBankTransactionUseCase {
    Mono<BankTransaction> registerTransaction(RegisterBankTransactionCommand command);
}
