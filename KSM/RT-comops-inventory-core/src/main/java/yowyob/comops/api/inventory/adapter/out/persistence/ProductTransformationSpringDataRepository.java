package yowyob.comops.api.inventory.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ProductTransformationSpringDataRepository
        extends ReactiveCrudRepository<ProductTransformationEntity, UUID> {

    reactor.core.publisher.Mono<ProductTransformationEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<ProductTransformationEntity> findAllByTenantIdAndOrganizationIdAndAgencyId(UUID tenantId,
            UUID organizationId, UUID agencyId);
}
