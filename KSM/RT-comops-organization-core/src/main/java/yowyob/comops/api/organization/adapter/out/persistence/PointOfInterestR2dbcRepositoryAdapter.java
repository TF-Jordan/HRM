package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.PointOfInterestRepository;
import yowyob.comops.api.organization.domain.model.PointOfInterest;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class PointOfInterestR2dbcRepositoryAdapter implements PointOfInterestRepository {

    private final PointOfInterestSpringDataRepository repository;

    public PointOfInterestR2dbcRepositoryAdapter(PointOfInterestSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PointOfInterest> save(PointOfInterest pointOfInterest) {
        PointOfInterestEntity entity = new PointOfInterestEntity(pointOfInterest.id(), pointOfInterest.tenantId(),
                pointOfInterest.createdAt(), pointOfInterest.updatedAt(), pointOfInterest.organizationId(),
                pointOfInterest.agencyId(), pointOfInterest.name(), pointOfInterest.poiType(), pointOfInterest.latitude(),
                pointOfInterest.longitude());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByAgencyAndName(UUID tenantId, UUID organizationId, UUID agencyId, String name) {
        return repository.existsByTenantIdAndOrganizationIdAndAgencyIdAndNameIgnoreCase(tenantId, organizationId,
                agencyId, name);
    }

    @Override
    public Flux<PointOfInterest> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findAllByTenantIdAndOrganizationIdAndAgencyId(tenantId, organizationId, agencyId)
                .map(this::toDomain);
    }

    @Override
    public Flux<PointOfInterest> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .map(this::toDomain);
    }

    @Override
    public Mono<PointOfInterest> findById(UUID tenantId, UUID pointOfInterestId) {
        return repository.findByIdAndTenantId(pointOfInterestId, tenantId)
                .map(this::toDomain);
    }

    private PointOfInterest toDomain(PointOfInterestEntity entity) {
        return PointOfInterest.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), entity.name(), entity.poiType(), entity.latitude(),
                entity.longitude());
    }
}
