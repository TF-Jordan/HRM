package yowyob.comops.api.accounting.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InvoiceSpringDataRepository extends ReactiveCrudRepository<InvoiceEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndInvoiceNumberIgnoreCase(UUID tenantId, UUID organizationId,
            String invoiceNumber);

    Mono<Boolean> existsByOrderId(UUID orderId);

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndInvoiceNumberIgnoreCaseAndIdNot(UUID tenantId, UUID organizationId,
            String invoiceNumber, UUID id);

    Flux<InvoiceEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);

    Mono<Void> deleteById(UUID id);
}
