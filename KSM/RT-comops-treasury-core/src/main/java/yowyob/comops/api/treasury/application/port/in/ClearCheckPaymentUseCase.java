package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.CheckPayment;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ClearCheckPaymentUseCase {
    Mono<CheckPayment> clear(UUID checkPaymentId);
}
