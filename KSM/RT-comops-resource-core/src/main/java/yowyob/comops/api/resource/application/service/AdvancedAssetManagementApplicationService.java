package yowyob.comops.api.resource.application.service;

import yowyob.comops.api.actor.application.port.out.ActorRepository;
import yowyob.comops.api.resource.application.port.in.ReleaseResourceReservationUseCase;
import yowyob.comops.api.organization.application.port.out.PhysicalSpaceRepository;
import yowyob.comops.api.resource.application.port.in.DisposeMaterialResourceUseCase;
import yowyob.comops.api.resource.application.port.in.UnassignMaterialResourceUseCase;
import yowyob.comops.api.resource.application.port.out.ResourceAssignmentRepository;
import yowyob.comops.api.resource.application.port.out.AssetProfileRepository;
import yowyob.comops.api.resource.application.port.out.MaterialResourceRepository;
import yowyob.comops.api.resource.application.port.out.ResourceReservationRepository;
import yowyob.comops.api.resource.domain.MaterialResourceNotFoundException;
import yowyob.comops.api.resource.domain.model.AssetProfile;
import yowyob.comops.api.resource.domain.model.MaterialResource;
import yowyob.comops.api.tp.application.port.out.ThirdPartyRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AdvancedAssetManagementApplicationService {

    private final MaterialResourceRepository materialResourceRepository;
    private final AssetProfileRepository assetProfileRepository;
    private final PhysicalSpaceRepository physicalSpaceRepository;
    private final ActorRepository actorRepository;
    private final ThirdPartyRepository thirdPartyRepository;
    private final DisposeMaterialResourceUseCase disposeMaterialResourceUseCase;
    private final UnassignMaterialResourceUseCase unassignMaterialResourceUseCase;
    private final ReleaseResourceReservationUseCase releaseResourceReservationUseCase;
    private final ResourceAssignmentRepository resourceAssignmentRepository;
    private final ResourceReservationRepository resourceReservationRepository;

    public AdvancedAssetManagementApplicationService(MaterialResourceRepository materialResourceRepository,
            AssetProfileRepository assetProfileRepository, PhysicalSpaceRepository physicalSpaceRepository,
            ActorRepository actorRepository, ThirdPartyRepository thirdPartyRepository,
            DisposeMaterialResourceUseCase disposeMaterialResourceUseCase,
            UnassignMaterialResourceUseCase unassignMaterialResourceUseCase,
            ReleaseResourceReservationUseCase releaseResourceReservationUseCase,
            ResourceAssignmentRepository resourceAssignmentRepository,
            ResourceReservationRepository resourceReservationRepository) {
        this.materialResourceRepository = materialResourceRepository;
        this.assetProfileRepository = assetProfileRepository;
        this.physicalSpaceRepository = physicalSpaceRepository;
        this.actorRepository = actorRepository;
        this.thirdPartyRepository = thirdPartyRepository;
        this.disposeMaterialResourceUseCase = disposeMaterialResourceUseCase;
        this.unassignMaterialResourceUseCase = unassignMaterialResourceUseCase;
        this.releaseResourceReservationUseCase = releaseResourceReservationUseCase;
        this.resourceAssignmentRepository = resourceAssignmentRepository;
        this.resourceReservationRepository = resourceReservationRepository;
    }

    public Mono<AssetProfile> getProfile(UUID tenantId, UUID resourceId) {
        return getResource(tenantId, resourceId)
                .flatMap(resource -> assetProfileRepository.findByResourceId(tenantId, resourceId)
                        .switchIfEmpty(assetProfileRepository.save(AssetProfile.defaults(tenantId,
                                resource.organizationId(), resource.agencyId(), resource.id(), resource.category()))));
    }

    public Mono<AssetProfile> upsertProfile(UUID tenantId, UUID resourceId, UpsertAssetProfileCommand command) {
        return getResource(tenantId, resourceId)
                .flatMap(resource -> validateReferences(tenantId, resource.organizationId(), resource.agencyId(),
                                command.physicalSpaceId(), command.ownerActorId(), command.supplierThirdPartyId())
                        .then(getProfile(tenantId, resourceId))
                        .map(existing -> existing.update(command.physicalSpaceId(), command.ownerActorId(),
                                command.supplierThirdPartyId(), command.assetClass(), command.criticality(),
                                command.lifecyclePhase(), command.complianceStatus(), command.acquisitionCost(),
                                command.currentValue(), command.depreciationMethod(), command.acquisitionDate(),
                                command.warrantyUntil(), command.expectedRenewalDate(),
                                command.lastComplianceCheckAt(), command.nextComplianceCheckAt(),
                                command.maintenanceContractReference(), command.notes()))
                        .flatMap(assetProfileRepository::save));
    }

    public Mono<AssetProfile> retireAsset(UUID tenantId, UUID resourceId, String notes) {
        return getResource(tenantId, resourceId)
                .flatMap(resource -> closeAssignmentRecord(tenantId, resourceId)
                        .then(closeReservationRecord(tenantId, resourceId))
                        .then(materialResourceRepository.save(toDisposedResource(resource))))
                .then(getProfile(tenantId, resourceId))
                .map(profile -> profile.markRetired(notes))
                .flatMap(assetProfileRepository::save);
    }

    private Mono<Void> closeAssignmentRecord(UUID tenantId, UUID resourceId) {
        return resourceAssignmentRepository.findActiveByTenantIdAndResourceId(tenantId, resourceId)
                .flatMap(assignment -> unassignMaterialResourceUseCase.unassign(tenantId, resourceId))
                .then();
    }

    private Mono<Void> closeReservationRecord(UUID tenantId, UUID resourceId) {
        return resourceReservationRepository.findActiveByTenantIdAndResourceId(tenantId, resourceId)
                .flatMap(reservation -> releaseResourceReservationUseCase.releaseReservation(tenantId,
                        resourceId, reservation.id()))
                .then();
    }

    private MaterialResource toDisposedResource(MaterialResource resource) {
        MaterialResource normalized = resource;
        if ("ASSIGNED".equals(normalized.status())) {
            normalized = normalized.unassign();
        }
        if ("RESERVED".equals(normalized.status())) {
            normalized = normalized.releaseReservation();
        }
        return normalized.dispose();
    }

    public Flux<AssetProfile> listOrganizationAssets(UUID tenantId, UUID organizationId) {
        return assetProfileRepository.findByOrganizationId(tenantId, organizationId)
                .sort(Comparator.comparing(AssetProfile::criticality)
                        .thenComparing(AssetProfile::lifecyclePhase)
                        .thenComparing(AssetProfile::resourceId));
    }

    public Mono<AdvancedAssetOverview> organizationOverview(UUID tenantId, UUID organizationId) {
        return assetProfileRepository.findByOrganizationId(tenantId, organizationId).collectList()
                .map(this::toOverview);
    }

    private AdvancedAssetOverview toOverview(List<AssetProfile> profiles) {
        long compliant = profiles.stream().filter(profile -> "COMPLIANT".equals(profile.complianceStatus())).count();
        long expiring = profiles.stream()
                .filter(profile -> profile.warrantyUntil() != null && profile.warrantyUntil().isBefore(Instant.now().plusSeconds(60L * 60 * 24 * 90)))
                .count();
        BigDecimal acquisition = profiles.stream().map(AssetProfile::acquisitionCost).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        BigDecimal current = profiles.stream().map(AssetProfile::currentValue).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        return new AdvancedAssetOverview(profiles.size(), compliant, profiles.size() - compliant, expiring,
                acquisition, current);
    }

    private Mono<Void> validateReferences(UUID tenantId, UUID organizationId, UUID agencyId, UUID physicalSpaceId,
            UUID ownerActorId, UUID supplierThirdPartyId) {
        Mono<Void> space = physicalSpaceId == null ? Mono.empty()
                : physicalSpaceRepository.findById(tenantId, physicalSpaceId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("physical space not found")))
                        .flatMap(physicalSpace -> physicalSpace.organizationId().equals(organizationId)
                                && physicalSpace.agencyId().equals(agencyId)
                                ? Mono.<Void>empty()
                                : Mono.error(new IllegalArgumentException(
                                        "physical space does not belong to the resource scope")));
        Mono<Void> owner = ownerActorId == null ? Mono.empty()
                : actorRepository.findById(tenantId, ownerActorId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("owner actor not found")))
                        .then();
        Mono<Void> supplier = supplierThirdPartyId == null ? Mono.empty()
                : thirdPartyRepository.findById(tenantId, supplierThirdPartyId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("supplier third party not found")))
                        .then();
        return Mono.when(space, owner, supplier);
    }

    private Mono<MaterialResource> getResource(UUID tenantId, UUID resourceId) {
        return materialResourceRepository.findById(resourceId)
                .filter(resource -> resource.tenantId().equals(tenantId))
                .switchIfEmpty(Mono.error(new MaterialResourceNotFoundException(resourceId)));
    }

    public record UpsertAssetProfileCommand(UUID physicalSpaceId, UUID ownerActorId, UUID supplierThirdPartyId,
            String assetClass, String criticality, String lifecyclePhase, String complianceStatus,
            BigDecimal acquisitionCost, BigDecimal currentValue, String depreciationMethod, Instant acquisitionDate,
            Instant warrantyUntil, Instant expectedRenewalDate, Instant lastComplianceCheckAt,
            Instant nextComplianceCheckAt, String maintenanceContractReference, String notes) {
    }

    public record AdvancedAssetOverview(int totalAssets, long compliantAssets, long nonCompliantAssets,
            long assetsWithExpiringWarranty, BigDecimal totalAcquisitionCost, BigDecimal totalCurrentValue) {
    }
}
