package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.BankTransaction;
import reactor.core.publisher.Mono;

public interface ManualReconcileBankTransactionUseCase {
    Mono<BankTransaction> reconcile(ManualReconcileBankTransactionCommand command);
}
