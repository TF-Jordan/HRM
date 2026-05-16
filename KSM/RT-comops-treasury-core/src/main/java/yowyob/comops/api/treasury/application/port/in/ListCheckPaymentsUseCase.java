package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.CheckPayment;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListCheckPaymentsUseCase {
    Flux<CheckPayment> listCheckPayments(UUID tenantId, UUID organizationId, UUID bankAccountId);
}
