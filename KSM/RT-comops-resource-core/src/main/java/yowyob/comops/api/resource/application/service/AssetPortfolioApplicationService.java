package yowyob.comops.api.resource.application.service;

import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.resource.application.port.out.MaintenanceRecordRepository;
import yowyob.comops.api.resource.application.port.out.MaterialResourceRepository;
import yowyob.comops.api.resource.application.port.out.ResourceAssignmentRepository;
import yowyob.comops.api.resource.application.port.out.ResourceReservationRepository;
import yowyob.comops.api.resource.domain.model.MaintenanceRecord;
import yowyob.comops.api.resource.domain.model.MaterialResource;
import yowyob.comops.api.resource.domain.model.ResourceAssignment;
import yowyob.comops.api.resource.domain.model.ResourceReservation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AssetPortfolioApplicationService {

    private final MaterialResourceRepository materialResourceRepository;
    private final ResourceAssignmentRepository resourceAssignmentRepository;
    private final ResourceReservationRepository resourceReservationRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final OrganizationRepository organizationRepository;
    private final AgencyRepository agencyRepository;

    public AssetPortfolioApplicationService(MaterialResourceRepository materialResourceRepository,
            ResourceAssignmentRepository resourceAssignmentRepository,
            ResourceReservationRepository resourceReservationRepository,
            MaintenanceRecordRepository maintenanceRecordRepository,
            OrganizationRepository organizationRepository,
            AgencyRepository agencyRepository) {
        this.materialResourceRepository = materialResourceRepository;
        this.resourceAssignmentRepository = resourceAssignmentRepository;
        this.resourceReservationRepository = resourceReservationRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.organizationRepository = organizationRepository;
        this.agencyRepository = agencyRepository;
    }

    public Mono<AssetPortfolioView> organizationPortfolio(UUID tenantId, UUID organizationId) {
        return buildPortfolio(tenantId, organizationId, null, "ORGANIZATION", organizationId);
    }

    public Mono<AssetPortfolioView> agencyPortfolio(UUID tenantId, UUID organizationId, UUID agencyId) {
        return validateAgencyScope(tenantId, organizationId, agencyId)
                .then(buildPortfolio(tenantId, organizationId, agencyId, "AGENCY", agencyId));
    }

    private Mono<AssetPortfolioView> buildPortfolio(UUID tenantId, UUID organizationId, UUID agencyId,
            String scopeType, UUID scopeId) {
        return validateOrganizationScope(tenantId, organizationId)
                .thenMany(materialResourceRepository.findByOrganizationId(tenantId, organizationId)
                        .filter(resource -> agencyId == null || resource.agencyId().equals(agencyId)))
                .flatMap(resource -> enrichResource(tenantId, resource))
                .collectList()
                .map(resources -> toPortfolio(scopeType, scopeId, organizationId, agencyId, resources));
    }

    private Mono<AssetResourceView> enrichResource(UUID tenantId, MaterialResource resource) {
        Mono<Optional<ResourceAssignment>> assignmentMono = resourceAssignmentRepository
                .findActiveByTenantIdAndResourceId(tenantId, resource.id())
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty());
        Mono<Optional<ResourceReservation>> reservationMono = resourceReservationRepository
                .findActiveByTenantIdAndResourceId(tenantId, resource.id())
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty());
        Mono<List<MaintenanceRecord>> maintenanceMono = maintenanceRecordRepository
                .findByTenantIdAndResourceId(tenantId, resource.id())
                .collectList();
        return Mono.zip(assignmentMono, reservationMono, maintenanceMono)
                .map(tuple -> {
                    List<MaintenanceRecord> maintenanceRecords = tuple.getT3();
                    long openMaintenanceCount = maintenanceRecords.stream()
                            .filter(record -> "OPEN".equals(record.status()))
                            .count();
                    ResourceAssignment assignment = tuple.getT1().orElse(null);
                    ResourceReservation reservation = tuple.getT2().orElse(null);
                    return new AssetResourceView(
                            resource.id(),
                            resource.organizationId(),
                            resource.agencyId(),
                            resource.resourceCode(),
                            resource.name(),
                            resource.category(),
                            resource.status(),
                            resource.ipAddress(),
                            resource.macAddress(),
                            toTargetView(assignment),
                            toTargetView(reservation),
                            maintenanceRecords.size(),
                            openMaintenanceCount);
                });
    }

    private AssetPortfolioView toPortfolio(String scopeType, UUID scopeId, UUID organizationId, UUID agencyId,
            List<AssetResourceView> resources) {
        Map<String, Long> countsByStatus = new LinkedHashMap<>();
        Map<String, Long> countsByCategory = new LinkedHashMap<>();
        long assigned = 0L;
        long reserved = 0L;
        long openMaintenance = 0L;
        for (AssetResourceView resource : resources) {
            countsByStatus.merge(resource.status(), 1L, Long::sum);
            countsByCategory.merge(resource.category(), 1L, Long::sum);
            if (resource.assignment() != null) {
                assigned++;
            }
            if (resource.reservation() != null) {
                reserved++;
            }
            openMaintenance += resource.openMaintenanceCount();
        }
        return new AssetPortfolioView(scopeType, scopeId, organizationId, agencyId, resources.size(), assigned,
                reserved, openMaintenance, countsByStatus, countsByCategory, resources);
    }

    private TargetView toTargetView(ResourceAssignment assignment) {
        if (assignment == null) {
            return null;
        }
        return new TargetView(assignment.assigneeType(), assignment.assigneeId(), assignment.assignedAt(),
                assignment.status());
    }

    private TargetView toTargetView(ResourceReservation reservation) {
        if (reservation == null) {
            return null;
        }
        return new TargetView(reservation.reserveeType(), reservation.reserveeId(), reservation.reservedAt(),
                reservation.status());
    }

    private Mono<Void> validateOrganizationScope(UUID tenantId, UUID organizationId) {
        return organizationRepository.findById(tenantId, organizationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("organization not found")))
                .then();
    }

    private Mono<Void> validateAgencyScope(UUID tenantId, UUID organizationId, UUID agencyId) {
        return validateOrganizationScope(tenantId, organizationId)
                .then(agencyRepository.findById(tenantId, agencyId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("agency not found")))
                        .flatMap(agency -> {
                            if (!agency.organizationId().equals(organizationId)) {
                                return Mono.error(new IllegalArgumentException("agency does not belong to the organization"));
                            }
                            return Mono.empty();
                        }));
    }

    public record AssetPortfolioView(String scopeType, UUID scopeId, UUID organizationId, UUID agencyId,
            int totalResources, long assignedResources, long reservedResources, long openMaintenanceCount,
            Map<String, Long> countsByStatus, Map<String, Long> countsByCategory, List<AssetResourceView> resources) {
    }

    public record AssetResourceView(UUID id, UUID organizationId, UUID agencyId, String resourceCode, String name,
            String category, String status, String ipAddress, String macAddress, TargetView assignment,
            TargetView reservation, int maintenanceCount, long openMaintenanceCount) {
    }

    public record TargetView(String targetType, UUID targetId, java.time.Instant at, String status) {
    }
}
