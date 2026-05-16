package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.BankTransaction;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListBankTransactionsUseCase {
    Flux<BankTransaction> listBankTransactions(UUID bankAccountId, int limit);
}
