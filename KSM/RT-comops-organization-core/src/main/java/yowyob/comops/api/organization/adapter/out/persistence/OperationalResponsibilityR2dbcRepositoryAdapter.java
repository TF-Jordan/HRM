package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.OperationalResponsibilityRepository;
import yowyob.comops.api.organization.domain.model.OperationalResponsibility;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class OperationalResponsibilityR2dbcRepositoryAdapter implements OperationalResponsibilityRepository {

    private final OperationalResponsibilitySpringDataRepository repository;

    public OperationalResponsibilityR2dbcRepositoryAdapter(OperationalResponsibilitySpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<OperationalResponsibility> save(OperationalResponsibility responsibility) {
        return repository.save(toEntity(responsibility)).map(this::toDomain);
    }

    @Override
    public Flux<OperationalResponsibility> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Flux<OperationalResponsibility> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findAllByTenantIdAndOrganizationIdAndAgencyId(tenantId, organizationId, agencyId)
                .map(this::toDomain);
    }

    @Override
    public Flux<OperationalResponsibility> findByPhysicalSpaceId(UUID tenantId, UUID physicalSpaceId) {
        return repository.findAllByTenantIdAndPhysicalSpaceId(tenantId, physicalSpaceId).map(this::toDomain);
    }

    private OperationalResponsibilityEntity toEntity(OperationalResponsibility responsibility) {
        return new OperationalResponsibilityEntity(responsibility.id(), responsibility.tenantId(),
                responsibility.createdAt(), responsibility.updatedAt(), responsibility.organizationId(),
                responsibility.agencyId(), responsibility.physicalSpaceId(), responsibility.actorId(),
                responsibility.responsibilityType(), responsibility.primaryResponsibility(),
                responsibility.active(), responsibility.notes());
    }

    private OperationalResponsibility toDomain(OperationalResponsibilityEntity entity) {
        return OperationalResponsibility.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(),
                entity.updatedAt(), entity.organizationId(), entity.agencyId(), entity.physicalSpaceId(),
                entity.actorId(), entity.responsibilityType(), entity.primaryResponsibility(), entity.active(),
                entity.notes());
    }
}
