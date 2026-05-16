package yowyob.comops.api.organization.application.service;

import yowyob.comops.api.actor.application.port.out.ActorRepository;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.OperationalResponsibilityRepository;
import yowyob.comops.api.organization.application.port.out.OperationalSiteProfileRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.organization.application.port.out.PhysicalSpaceRepository;
import yowyob.comops.api.organization.domain.model.Agency;
import yowyob.comops.api.organization.domain.model.OperationalResponsibility;
import yowyob.comops.api.organization.domain.model.OperationalSiteProfile;
import yowyob.comops.api.organization.domain.model.PhysicalSpace;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OperationalSiteGovernanceApplicationService {

    private final OrganizationRepository organizationRepository;
    private final AgencyRepository agencyRepository;
    private final PhysicalSpaceRepository physicalSpaceRepository;
    private final ActorRepository actorRepository;
    private final OperationalSiteProfileRepository operationalSiteProfileRepository;
    private final OperationalResponsibilityRepository operationalResponsibilityRepository;

    public OperationalSiteGovernanceApplicationService(OrganizationRepository organizationRepository,
            AgencyRepository agencyRepository, PhysicalSpaceRepository physicalSpaceRepository,
            ActorRepository actorRepository, OperationalSiteProfileRepository operationalSiteProfileRepository,
            OperationalResponsibilityRepository operationalResponsibilityRepository) {
        this.organizationRepository = organizationRepository;
        this.agencyRepository = agencyRepository;
        this.physicalSpaceRepository = physicalSpaceRepository;
        this.actorRepository = actorRepository;
        this.operationalSiteProfileRepository = operationalSiteProfileRepository;
        this.operationalResponsibilityRepository = operationalResponsibilityRepository;
    }

    public Mono<OperationalSiteProfile> getSiteProfile(UUID tenantId, UUID organizationId, UUID agencyId) {
        return validateAgencyScope(tenantId, organizationId, agencyId)
                .flatMap(agency -> operationalSiteProfileRepository.findByAgencyId(tenantId, organizationId, agencyId)
                        .switchIfEmpty(Mono.defer(() -> operationalSiteProfileRepository
                                .save(OperationalSiteProfile.defaults(tenantId, organizationId, agencyId,
                                        agency.agencyType())))));
    }

    public Mono<OperationalSiteProfile> upsertSiteProfile(UUID tenantId, UUID organizationId, UUID agencyId,
            UpsertOperationalSiteProfileCommand command) {
        return validateAgencyScope(tenantId, organizationId, agencyId)
                .flatMap(agency -> validateDefaultSpace(tenantId, organizationId, agencyId,
                                command.defaultPhysicalSpaceId())
                        .then(operationalSiteProfileRepository.findByAgencyId(tenantId, organizationId, agencyId)
                                .defaultIfEmpty(OperationalSiteProfile.defaults(tenantId, organizationId, agencyId,
                                        agency.agencyType()))
                                .map(existing -> existing.update(command.siteCategory(), command.operatingModel(),
                                        command.openingStatus(), command.cashEnabled(), command.warehouseEnabled(),
                                        command.maintenanceEnabled(), command.inventoryEnabled(),
                                        command.documentComplianceRequired(), command.defaultPhysicalSpaceId(),
                                        command.readinessNotes(), command.commissionedAt()))
                                .flatMap(operationalSiteProfileRepository::save)));
    }

    public Mono<OperationalResponsibility> assignResponsibility(UUID tenantId, UUID organizationId, UUID agencyId,
            AssignOperationalResponsibilityCommand command) {
        return validateAgencyScope(tenantId, organizationId, agencyId)
                .then(validateDefaultSpace(tenantId, organizationId, agencyId, command.physicalSpaceId()))
                .then(actorRepository.findById(tenantId, command.actorId())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("actor not found"))))
                .then(operationalResponsibilityRepository.save(OperationalResponsibility.assign(tenantId,
                        organizationId, agencyId, command.physicalSpaceId(), command.actorId(),
                        command.responsibilityType(), command.primaryResponsibility(), command.active(),
                        command.notes())));
    }

    public Flux<OperationalResponsibility> listResponsibilities(UUID tenantId, UUID organizationId, UUID agencyId,
            UUID physicalSpaceId) {
        if (physicalSpaceId != null) {
            return validateDefaultSpace(tenantId, organizationId, agencyId, physicalSpaceId)
                    .thenMany(operationalResponsibilityRepository.findByPhysicalSpaceId(tenantId, physicalSpaceId))
                    .sort(Comparator.comparing(OperationalResponsibility::responsibilityType)
                            .thenComparing(OperationalResponsibility::actorId));
        }
        return validateAgencyScope(tenantId, organizationId, agencyId)
                .thenMany(operationalResponsibilityRepository.findByAgencyId(tenantId, organizationId, agencyId)
                        .sort(Comparator.comparing(OperationalResponsibility::responsibilityType)
                                .thenComparing(OperationalResponsibility::actorId)));
    }

    public Mono<OperationalSiteReadinessView> readiness(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Mono.zip(getSiteProfile(tenantId, organizationId, agencyId),
                        physicalSpaceRepository.findByAgencyId(tenantId, organizationId, agencyId).collectList(),
                        operationalResponsibilityRepository.findByAgencyId(tenantId, organizationId, agencyId)
                                .collectList())
                .map(tuple -> {
                    OperationalSiteProfile profile = tuple.getT1();
                    List<PhysicalSpace> spaces = tuple.getT2();
                    List<OperationalResponsibility> responsibilities = tuple.getT3();
                    long activeSpaces = spaces.stream().filter(PhysicalSpace::active).count();
                    long primaryResponsibilities = responsibilities.stream()
                            .filter(OperationalResponsibility::active)
                            .filter(OperationalResponsibility::primaryResponsibility)
                            .count();
                    boolean ready = activeSpaces > 0 && primaryResponsibilities > 0
                            && !"CLOSED".equals(profile.openingStatus());
                    return new OperationalSiteReadinessView(profile.organizationId(), profile.agencyId(),
                            profile.openingStatus(), spaces.size(), activeSpaces, responsibilities.size(),
                            primaryResponsibilities, ready,
                            ready ? "SITE_READY" : "SITE_REQUIRES_SPACES_AND_PRIMARY_RESPONSIBILITIES");
                });
    }

    private Mono<Agency> validateAgencyScope(UUID tenantId, UUID organizationId, UUID agencyId) {
        return organizationRepository.findById(tenantId, organizationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("organization not found")))
                .then(agencyRepository.findById(tenantId, agencyId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("agency not found")))
                        .flatMap(agency -> agency.organizationId().equals(organizationId)
                                ? Mono.just(agency)
                                : Mono.error(new IllegalArgumentException(
                                        "agency does not belong to organization"))));
    }

    private Mono<Void> validateDefaultSpace(UUID tenantId, UUID organizationId, UUID agencyId, UUID physicalSpaceId) {
        if (physicalSpaceId == null) {
            return Mono.empty();
        }
        return physicalSpaceRepository.findById(tenantId, physicalSpaceId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("physical space not found")))
                .flatMap(space -> belongsToScope(space, organizationId, agencyId)
                        ? Mono.empty()
                        : Mono.error(new IllegalArgumentException(
                                "physical space does not belong to agency scope")));
    }

    private boolean belongsToScope(PhysicalSpace space, UUID organizationId, UUID agencyId) {
        return space.organizationId().equals(organizationId) && space.agencyId().equals(agencyId);
    }

    public record UpsertOperationalSiteProfileCommand(String siteCategory, String operatingModel,
            String openingStatus, boolean cashEnabled, boolean warehouseEnabled, boolean maintenanceEnabled,
            boolean inventoryEnabled, boolean documentComplianceRequired, UUID defaultPhysicalSpaceId,
            String readinessNotes, Instant commissionedAt) {
    }

    public record AssignOperationalResponsibilityCommand(UUID physicalSpaceId, UUID actorId,
            String responsibilityType, boolean primaryResponsibility, boolean active, String notes) {
    }

    public record OperationalSiteReadinessView(UUID organizationId, UUID agencyId, String openingStatus,
            int totalPhysicalSpaces, long activePhysicalSpaces, int totalResponsibilities,
            long primaryResponsibilities, boolean ready, String readinessStatus) {
    }
}
