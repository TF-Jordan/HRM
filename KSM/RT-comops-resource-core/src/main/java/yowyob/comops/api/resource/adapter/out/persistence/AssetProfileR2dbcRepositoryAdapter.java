package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.resource.application.port.out.AssetProfileRepository;
import yowyob.comops.api.resource.domain.model.AssetProfile;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class AssetProfileR2dbcRepositoryAdapter implements AssetProfileRepository {

    private final AssetProfileSpringDataRepository repository;

    public AssetProfileR2dbcRepositoryAdapter(AssetProfileSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<AssetProfile> save(AssetProfile assetProfile) {
        return repository.save(toEntity(assetProfile)).map(this::toDomain);
    }

    @Override
    public Mono<AssetProfile> findByResourceId(UUID tenantId, UUID resourceId) {
        return repository.findByTenantIdAndResourceId(tenantId, resourceId).map(this::toDomain);
    }

    @Override
    public Flux<AssetProfile> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    private AssetProfileEntity toEntity(AssetProfile profile) {
        return new AssetProfileEntity(profile.id(), profile.tenantId(), profile.createdAt(), profile.updatedAt(),
                profile.organizationId(), profile.agencyId(), profile.resourceId(), profile.physicalSpaceId(),
                profile.ownerActorId(), profile.supplierThirdPartyId(), profile.assetClass(), profile.criticality(),
                profile.lifecyclePhase(), profile.complianceStatus(), profile.acquisitionCost(),
                profile.currentValue(), profile.depreciationMethod(), profile.acquisitionDate(),
                profile.warrantyUntil(), profile.expectedRenewalDate(), profile.lastComplianceCheckAt(),
                profile.nextComplianceCheckAt(), profile.maintenanceContractReference(), profile.notes());
    }

    private AssetProfile toDomain(AssetProfileEntity entity) {
        return AssetProfile.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), entity.resourceId(), entity.physicalSpaceId(),
                entity.ownerActorId(), entity.supplierThirdPartyId(), entity.assetClass(), entity.criticality(),
                entity.lifecyclePhase(), entity.complianceStatus(), entity.acquisitionCost(), entity.currentValue(),
                entity.depreciationMethod(), entity.acquisitionDate(), entity.warrantyUntil(),
                entity.expectedRenewalDate(), entity.lastComplianceCheckAt(), entity.nextComplianceCheckAt(),
                entity.maintenanceContractReference(), entity.notes());
    }
}
