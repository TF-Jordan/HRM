package yowyob.comops.api.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PointOfInterestLinkSpringDataRepository extends ReactiveCrudRepository<PointOfInterestLinkEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndAgencyIdAndPointOfInterestId(UUID tenantId, UUID agencyId, UUID pointOfInterestId);

    Flux<PointOfInterestLinkEntity> findAllByTenantIdAndAgencyId(UUID tenantId, UUID agencyId);

    Mono<Void> deleteByTenantIdAndAgencyIdAndPointOfInterestId(UUID tenantId, UUID agencyId, UUID pointOfInterestId);
}
