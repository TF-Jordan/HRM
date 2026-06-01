package yowyob.comops.api.treasury.application.port.out;

import yowyob.comops.api.treasury.domain.model.CheckPayment;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CheckPaymentRepository {
    Mono<CheckPayment> findById(UUID checkPaymentId);
    Flux<CheckPayment> findByOrganizationId(UUID tenantId, UUID organizationId);
    Flux<CheckPayment> findByBankAccountId(UUID tenantId, UUID bankAccountId);
    Mono<CheckPayment> save(CheckPayment checkPayment);
}
