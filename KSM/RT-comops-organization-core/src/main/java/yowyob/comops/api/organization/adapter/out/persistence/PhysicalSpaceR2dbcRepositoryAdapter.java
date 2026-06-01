package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.PhysicalSpaceRepository;
import yowyob.comops.api.organization.domain.model.PhysicalSpace;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class PhysicalSpaceR2dbcRepositoryAdapter implements PhysicalSpaceRepository {

    private final PhysicalSpaceSpringDataRepository repository;

    public PhysicalSpaceR2dbcRepositoryAdapter(PhysicalSpaceSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PhysicalSpace> save(PhysicalSpace physicalSpace) {
        PhysicalSpaceEntity entity = new PhysicalSpaceEntity(physicalSpace.id(), physicalSpace.tenantId(),
                physicalSpace.createdAt(), physicalSpace.updatedAt(), physicalSpace.organizationId(),
                physicalSpace.agencyId(), physicalSpace.parentSpaceId(), physicalSpace.code(), physicalSpace.name(),
                physicalSpace.spaceType(), physicalSpace.description(), physicalSpace.levelNumber(),
                physicalSpace.capacity(), physicalSpace.active());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByAgencyAndCode(UUID tenantId, UUID organizationId, UUID agencyId, String code) {
        return repository.existsByTenantIdAndOrganizationIdAndAgencyIdAndCodeIgnoreCase(tenantId, organizationId,
                agencyId, code);
    }

    @Override
    public Flux<PhysicalSpace> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findAllByTenantIdAndOrganizationIdAndAgencyId(tenantId, organizationId, agencyId)
                .map(this::toDomain);
    }

    @Override
    public Flux<PhysicalSpace> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .map(this::toDomain);
    }

    @Override
    public Mono<PhysicalSpace> findById(UUID tenantId, UUID physicalSpaceId) {
        return repository.findByIdAndTenantId(physicalSpaceId, tenantId)
                .map(this::toDomain);
    }

    private PhysicalSpace toDomain(PhysicalSpaceEntity entity) {
        return PhysicalSpace.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), entity.parentSpaceId(), entity.code(), entity.name(),
                entity.spaceType(), entity.description(), entity.levelNumber(), entity.capacity(), entity.active());
    }
}
