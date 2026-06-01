package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.CheckPayment;
import reactor.core.publisher.Mono;

public interface RegisterCheckPaymentUseCase { Mono<CheckPayment> register(RegisterCheckPaymentCommand command); }
