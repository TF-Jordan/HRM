package yowyob.comops.api.organization.application.port.out;

import yowyob.comops.api.organization.domain.model.Organization;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationRepository {

    Mono<Boolean> existsByCode(UUID tenantId, String code);

    Mono<Boolean> existsByCodeExcludingId(UUID tenantId, String code, UUID organizationId);

    Mono<Organization> findById(UUID tenantId, UUID organizationId);

    Flux<Organization> findByTenantId(UUID tenantId);

    Flux<Organization> findByBusinessActorId(UUID tenantId, UUID businessActorId);

    Mono<Organization> save(Organization organization);

    Mono<Void> deleteById(UUID tenantId, UUID organizationId);
}
