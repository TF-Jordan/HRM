package yowyob.comops.api.sales.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SalesOrderSpringDataRepository extends ReactiveCrudRepository<SalesOrderEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndOrderNumberIgnoreCase(UUID tenantId, UUID organizationId, String orderNumber);

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndOrderNumberIgnoreCaseAndIdNot(UUID tenantId, UUID organizationId,
            String orderNumber, UUID id);

    Flux<SalesOrderEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);

    Mono<Void> deleteById(UUID id);
}
