package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.PointOfInterestLinkRepository;
import yowyob.comops.api.organization.domain.model.PointOfInterestLink;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class PointOfInterestLinkR2dbcRepositoryAdapter implements PointOfInterestLinkRepository {

    private final PointOfInterestLinkSpringDataRepository repository;

    public PointOfInterestLinkR2dbcRepositoryAdapter(PointOfInterestLinkSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PointOfInterestLink> save(PointOfInterestLink link) {
        return repository.save(toEntity(link)).map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByAgencyIdAndPointOfInterestId(UUID tenantId, UUID agencyId, UUID pointOfInterestId) {
        return repository.existsByTenantIdAndAgencyIdAndPointOfInterestId(tenantId, agencyId, pointOfInterestId);
    }

    @Override
    public Flux<PointOfInterestLink> findByAgencyId(UUID tenantId, UUID agencyId) {
        return repository.findAllByTenantIdAndAgencyId(tenantId, agencyId).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteByAgencyIdAndPointOfInterestId(UUID tenantId, UUID agencyId, UUID pointOfInterestId) {
        return repository.deleteByTenantIdAndAgencyIdAndPointOfInterestId(tenantId, agencyId, pointOfInterestId);
    }

    private PointOfInterestLinkEntity toEntity(PointOfInterestLink link) {
        return new PointOfInterestLinkEntity(link.id(), link.tenantId(), link.createdAt(), link.updatedAt(),
                link.organizationId(), link.agencyId(), link.pointOfInterestId(), link.distanceMeters(),
                link.description());
    }

    private PointOfInterestLink toDomain(PointOfInterestLinkEntity entity) {
        return PointOfInterestLink.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), entity.pointOfInterestId(), entity.distanceMeters(),
                entity.description());
    }
}
