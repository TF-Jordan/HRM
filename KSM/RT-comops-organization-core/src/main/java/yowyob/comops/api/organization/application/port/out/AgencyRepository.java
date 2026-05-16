package yowyob.comops.api.organization.application.port.out;

import yowyob.comops.api.organization.domain.model.Agency;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AgencyRepository {

    Mono<Boolean> existsByCode(UUID tenantId, UUID organizationId, String code);

    Mono<Agency> findById(UUID tenantId, UUID agencyId);

    Flux<Agency> findByOrganizationId(UUID tenantId, UUID organizationId);

    Flux<Agency> findByTenantId(UUID tenantId);

    Mono<Agency> save(Agency agency);
}
