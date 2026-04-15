package yowyob.comops.api.treasury.application.port.in;

import reactor.core.publisher.Mono;

public interface AutoReconcileBankTransactionsUseCase {
    Mono<ReconciliationRunResult> autoReconcile(java.util.UUID bankAccountId);
}
