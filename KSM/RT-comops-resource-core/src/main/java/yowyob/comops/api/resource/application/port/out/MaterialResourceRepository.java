package yowyob.comops.api.resource.application.port.out;

import yowyob.comops.api.resource.domain.model.MaterialResource;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MaterialResourceRepository {

    Mono<Boolean> existsByCode(UUID tenantId, UUID organizationId, String resourceCode);

    Mono<MaterialResource> findById(UUID resourceId);

    Flux<MaterialResource> findByOrganizationId(UUID tenantId, UUID organizationId);

    Mono<MaterialResource> save(MaterialResource materialResource);
}
