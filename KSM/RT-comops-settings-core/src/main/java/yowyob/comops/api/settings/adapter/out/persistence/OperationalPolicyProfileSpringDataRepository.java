package yowyob.comops.api.settings.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface OperationalPolicyProfileSpringDataRepository
        extends ReactiveCrudRepository<OperationalPolicyProfileEntity, UUID> {

    Mono<OperationalPolicyProfileEntity> findByTenantIdAndOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId,
            UUID agencyId);
}
