package yowyob.comops.api.treasury.application.port.out;

import yowyob.comops.api.treasury.domain.model.InvoiceSettlement;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InvoiceSettlementRepository {

    Mono<Boolean> existsBySettlementNumber(UUID tenantId, UUID organizationId, String settlementNumber);

    Mono<InvoiceSettlement> findById(UUID settlementId);

    Flux<InvoiceSettlement> findByOrganizationId(UUID tenantId, UUID organizationId);

    Mono<InvoiceSettlement> save(InvoiceSettlement settlement);
}
