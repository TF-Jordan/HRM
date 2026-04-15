package yowyob.comops.api.treasury.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InvoiceSettlementSpringDataRepository extends ReactiveCrudRepository<InvoiceSettlementEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndSettlementNumberIgnoreCase(UUID tenantId, UUID organizationId,
            String settlementNumber);

    Flux<InvoiceSettlementEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
