package yowyob.comops.api.inventory.application.port.out;

import yowyob.comops.api.inventory.domain.model.ProductTransformation;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductTransformationRepository {
    Mono<ProductTransformation> save(ProductTransformation transformation);
    Mono<ProductTransformation> findById(UUID tenantId, UUID transformationId);
    Flux<ProductTransformation> findByAgency(UUID tenantId, UUID organizationId, UUID agencyId);
}
