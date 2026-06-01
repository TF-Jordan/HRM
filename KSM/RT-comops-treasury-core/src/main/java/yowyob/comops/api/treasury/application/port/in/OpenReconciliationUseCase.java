package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.Reconciliation;
import reactor.core.publisher.Mono;

public interface OpenReconciliationUseCase { Mono<Reconciliation> open(OpenReconciliationCommand command); }
