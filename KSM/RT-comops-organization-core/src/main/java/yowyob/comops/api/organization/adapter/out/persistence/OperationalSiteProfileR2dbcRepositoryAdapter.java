package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.OperationalSiteProfileRepository;
import yowyob.comops.api.organization.domain.model.OperationalSiteProfile;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class OperationalSiteProfileR2dbcRepositoryAdapter implements OperationalSiteProfileRepository {

    private final OperationalSiteProfileSpringDataRepository repository;

    public OperationalSiteProfileR2dbcRepositoryAdapter(OperationalSiteProfileSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<OperationalSiteProfile> save(OperationalSiteProfile profile) {
        return repository.save(toEntity(profile)).map(this::toDomain);
    }

    @Override
    public Mono<OperationalSiteProfile> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findByTenantIdAndOrganizationIdAndAgencyId(tenantId, organizationId, agencyId)
                .map(this::toDomain);
    }

    @Override
    public Flux<OperationalSiteProfile> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    private OperationalSiteProfileEntity toEntity(OperationalSiteProfile profile) {
        return new OperationalSiteProfileEntity(profile.id(), profile.tenantId(), profile.createdAt(),
                profile.updatedAt(), profile.organizationId(), profile.agencyId(), profile.siteCategory(),
                profile.operatingModel(), profile.openingStatus(), profile.cashEnabled(), profile.warehouseEnabled(),
                profile.maintenanceEnabled(), profile.inventoryEnabled(), profile.documentComplianceRequired(),
                profile.defaultPhysicalSpaceId(), profile.readinessNotes(), profile.commissionedAt());
    }

    private OperationalSiteProfile toDomain(OperationalSiteProfileEntity entity) {
        return OperationalSiteProfile.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), entity.siteCategory(), entity.operatingModel(),
                entity.openingStatus(), entity.cashEnabled(), entity.warehouseEnabled(), entity.maintenanceEnabled(),
                entity.inventoryEnabled(), entity.documentComplianceRequired(), entity.defaultPhysicalSpaceId(),
                entity.readinessNotes(), entity.commissionedAt());
    }
}
