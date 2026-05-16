package yowyob.comops.api.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CertificationSpringDataRepository extends ReactiveCrudRepository<CertificationEntity, UUID> {

    Flux<CertificationEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
