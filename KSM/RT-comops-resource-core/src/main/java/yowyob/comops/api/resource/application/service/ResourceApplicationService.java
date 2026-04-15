package yowyob.comops.api.resource.application.service;

import yowyob.comops.api.resource.application.port.in.AssignMaterialResourceCommand;
import yowyob.comops.api.resource.application.port.in.AssignMaterialResourceUseCase;
import yowyob.comops.api.resource.application.port.in.DisposeMaterialResourceUseCase;
import yowyob.comops.api.resource.application.port.in.GetMaterialResourceUseCase;
import yowyob.comops.api.resource.application.port.in.ListLocationObservationsUseCase;
import yowyob.comops.api.resource.application.port.in.ListMaintenanceRecordsUseCase;
import yowyob.comops.api.resource.application.port.in.ListMaterialResourcesUseCase;
import yowyob.comops.api.resource.application.port.in.ListNetworkObservationsUseCase;
import yowyob.comops.api.resource.application.port.in.ListResourceAssignmentsUseCase;
import yowyob.comops.api.resource.application.port.in.ListResourceReservationsUseCase;
import yowyob.comops.api.resource.application.port.in.RecordLocationObservationCommand;
import yowyob.comops.api.resource.application.port.in.RecordLocationObservationUseCase;
import yowyob.comops.api.resource.application.port.in.RecordMaintenanceCommand;
import yowyob.comops.api.resource.application.port.in.RecordMaintenanceUseCase;
import yowyob.comops.api.resource.application.port.in.RecordNetworkObservationCommand;
import yowyob.comops.api.resource.application.port.in.RecordNetworkObservationUseCase;
import yowyob.comops.api.resource.application.port.in.RegisterMaterialResourceCommand;
import yowyob.comops.api.resource.application.port.in.RegisterMaterialResourceUseCase;
import yowyob.comops.api.resource.application.port.in.ReleaseResourceReservationUseCase;
import yowyob.comops.api.resource.application.port.in.ReserveMaterialResourceCommand;
import yowyob.comops.api.resource.application.port.in.ReserveMaterialResourceUseCase;
import yowyob.comops.api.resource.application.port.in.SearchMaterialResourcesUseCase;
import yowyob.comops.api.resource.application.port.in.UnassignMaterialResourceUseCase;
import yowyob.comops.api.resource.application.port.out.MaintenanceRecordRepository;
import yowyob.comops.api.resource.application.port.out.MaterialResourceRepository;
import yowyob.comops.api.resource.application.port.out.ResourceAssignmentRepository;
import yowyob.comops.api.resource.application.port.out.ResourceLocationObservationRepository;
import yowyob.comops.api.resource.application.port.out.ResourceNetworkObservationRepository;
import yowyob.comops.api.resource.application.port.out.ResourceReservationRepository;
import yowyob.comops.api.resource.application.port.out.ResourceSearchGateway;
import yowyob.comops.api.resource.domain.DuplicateResourceCodeException;
import yowyob.comops.api.resource.domain.MaterialResourceNotFoundException;
import yowyob.comops.api.resource.domain.ResourceAssignmentNotFoundException;
import yowyob.comops.api.resource.domain.ResourceConsistencyException;
import yowyob.comops.api.resource.domain.ResourceSearchUnavailableException;
import yowyob.comops.api.resource.domain.ResourceReservationNotFoundException;
import yowyob.comops.api.resource.domain.model.ResourceReservation;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.actor.application.port.out.ActorRepository;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.organization.application.port.out.PhysicalSpaceRepository;
import yowyob.comops.api.resource.domain.model.MaintenanceRecord;
import yowyob.comops.api.resource.domain.model.MaterialResource;
import yowyob.comops.api.resource.domain.model.MaterialResourceSearchResult;
import yowyob.comops.api.resource.domain.model.ResourceAssignment;
import yowyob.comops.api.resource.domain.model.ResourceLocationObservation;
import yowyob.comops.api.resource.domain.model.ResourceNetworkObservation;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ResourceApplicationService implements RegisterMaterialResourceUseCase, GetMaterialResourceUseCase,
        ListMaterialResourcesUseCase, ReserveMaterialResourceUseCase, ReleaseResourceReservationUseCase,
        AssignMaterialResourceUseCase, UnassignMaterialResourceUseCase, DisposeMaterialResourceUseCase,
        ListResourceAssignmentsUseCase, ListResourceReservationsUseCase,
        RecordMaintenanceUseCase, ListMaintenanceRecordsUseCase, RecordNetworkObservationUseCase,
        ListNetworkObservationsUseCase, RecordLocationObservationUseCase, ListLocationObservationsUseCase,
        SearchMaterialResourcesUseCase {

    private final MaterialResourceRepository materialResourceRepository;
    private final ResourceAssignmentRepository resourceAssignmentRepository;
    private final ResourceReservationRepository resourceReservationRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final ResourceNetworkObservationRepository resourceNetworkObservationRepository;
    private final ResourceLocationObservationRepository resourceLocationObservationRepository;
    private final ActorRepository actorRepository;
    private final OrganizationRepository organizationRepository;
    private final AgencyRepository agencyRepository;
    private final PhysicalSpaceRepository physicalSpaceRepository;
    private final Optional<ResourceSearchGateway> resourceSearchGateway;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public ResourceApplicationService(MaterialResourceRepository materialResourceRepository,
            ResourceAssignmentRepository resourceAssignmentRepository,
            ResourceReservationRepository resourceReservationRepository,
            MaintenanceRecordRepository maintenanceRecordRepository,
            ResourceNetworkObservationRepository resourceNetworkObservationRepository,
            ResourceLocationObservationRepository resourceLocationObservationRepository,
            ActorRepository actorRepository,
            OrganizationRepository organizationRepository,
            AgencyRepository agencyRepository,
            PhysicalSpaceRepository physicalSpaceRepository,
            Optional<ResourceSearchGateway> resourceSearchGateway,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.materialResourceRepository = materialResourceRepository;
        this.resourceAssignmentRepository = resourceAssignmentRepository;
        this.resourceReservationRepository = resourceReservationRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.resourceNetworkObservationRepository = resourceNetworkObservationRepository;
        this.resourceLocationObservationRepository = resourceLocationObservationRepository;
        this.actorRepository = actorRepository;
        this.organizationRepository = organizationRepository;
        this.agencyRepository = agencyRepository;
        this.physicalSpaceRepository = physicalSpaceRepository;
        this.resourceSearchGateway = resourceSearchGateway;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<MaterialResource> register(RegisterMaterialResourceCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<MaterialResource> operation = validateResourceScope(command.tenantId(), command.organizationId(),
                        command.agencyId())
                .then(Mono.fromSupplier(() -> MaterialResource.register(command.tenantId(), command.organizationId(),
                        command.agencyId(), command.resourceCode(), command.name(), command.category(),
                        command.serialNumber(), command.latitude(), command.longitude(), command.ipAddress(),
                        command.macAddress())))
                .flatMap(materialResource -> materialResourceRepository.existsByCode(materialResource.tenantId(),
                                materialResource.organizationId(),
                                materialResource.resourceCode())
                        .flatMap(exists -> exists
                                ? Mono.error(new DuplicateResourceCodeException(materialResource.resourceCode()))
                                : materialResourceRepository.save(materialResource)
                                        .flatMap(saved -> publish(resourceRegisteredEvent(saved)).thenReturn(saved))));
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<MaterialResource> reserve(UUID tenantId, ReserveMaterialResourceCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<MaterialResource> operation = getExistingResource(command.resourceId())
                .flatMap(resource -> validateTargetScope(tenantId, resource.organizationId(), command.reserveeType(),
                                command.reserveeId(), true)
                        .then(resourceReservationRepository.findActiveByTenantIdAndResourceId(tenantId,
                                command.resourceId())
                                .flatMap(existing -> Mono.<MaterialResource>error(new ResourceConsistencyException(
                                        "Resource already has an active reservation: " + existing.id())))
                                .switchIfEmpty(Mono.defer(() -> materialResourceRepository.save(resource.reserve())
                                        .flatMap(saved -> resourceReservationRepository.save(ResourceReservation.reserve(
                                                        tenantId, saved.id(), command.reserveeType(),
                                                        command.reserveeId(), command.reason()))
                                                .flatMap(reservation -> publish(resourceReservedEvent(saved, reservation))
                                                        .thenReturn(saved)))))));
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<MaterialResource> getResource(UUID resourceId) {
        return materialResourceRepository.findById(resourceId)
                .switchIfEmpty(Mono.error(new MaterialResourceNotFoundException(resourceId)));
    }

    @Override
    public Flux<MaterialResource> listResources(UUID tenantId, UUID organizationId, UUID agencyId, String category,
            String status) {
        String normalizedCategory = category == null || category.isBlank() ? null : category.trim().toUpperCase();
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim().toUpperCase();
        return validateResourceScope(tenantId, organizationId, agencyId)
                .thenMany(materialResourceRepository.findByOrganizationId(tenantId, organizationId)
                        .filter(resource -> agencyId == null || resource.agencyId().equals(agencyId))
                        .filter(resource -> normalizedCategory == null || resource.category().equals(normalizedCategory))
                        .filter(resource -> normalizedStatus == null || resource.status().equals(normalizedStatus)));
    }

    @Override
    public Flux<MaterialResourceSearchResult> searchResources(UUID tenantId, UUID organizationId, UUID agencyId,
            String query, String category, String status) {
        return resourceSearchGateway
                .map(gateway -> validateResourceScope(tenantId, organizationId, agencyId)
                        .thenMany(gateway.search(tenantId, organizationId, agencyId, query, category, status)))
                .orElseGet(() -> listResources(tenantId, organizationId, agencyId, category, status)
                        .filter(resource -> matchesQuery(resource, query))
                        .map(this::toSearchResult));
    }

    @Override
    public Mono<MaterialResource> releaseReservation(UUID tenantId, UUID resourceId, UUID reservationId) {
        return transactionalExecutor.transactional(Mono.zip(getExistingResource(resourceId),
                        resourceReservationRepository.findById(reservationId)
                                .switchIfEmpty(Mono.error(new ResourceReservationNotFoundException(reservationId))))
                .flatMap(tuple -> {
                    MaterialResource resource = tuple.getT1();
                    ResourceReservation reservation = tuple.getT2();
                    if (!reservation.resourceId().equals(resource.id())) {
                        return Mono.error(new ResourceConsistencyException(
                                "Reservation does not belong to resource " + resource.id()));
                    }
                    ResourceReservation releasedReservation = reservation.release();
                    return materialResourceRepository.save(resource.releaseReservation())
                            .flatMap(saved -> resourceReservationRepository.save(releasedReservation)
                                    .flatMap(ignored -> publish(resourceReservationReleasedEvent(saved,
                                            releasedReservation)).thenReturn(saved)));
                }));
    }

    @Override
    public Mono<MaterialResource> assign(UUID tenantId, AssignMaterialResourceCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<MaterialResource> operation = getExistingResource(command.resourceId())
                .flatMap(resource -> validateTargetScope(tenantId, resource.organizationId(), command.assigneeType(),
                                command.assigneeId(), true)
                        .then(resourceReservationRepository.findActiveByTenantIdAndResourceId(tenantId,
                                command.resourceId())
                                .flatMap(reservation -> {
                                    if (!reservation.reserveeType().equalsIgnoreCase(command.assigneeType())
                                            || !reservation.reserveeId().equals(command.assigneeId())) {
                                        return Mono.error(new ResourceConsistencyException(
                                                "Active reservation must match assignment target"));
                                    }
                                    ResourceReservation fulfilledReservation = reservation.fulfill();
                                    return materialResourceRepository.save(resource.assign())
                                            .flatMap(saved -> resourceReservationRepository.save(fulfilledReservation)
                                                    .then(resourceAssignmentRepository.save(ResourceAssignment.assign(
                                                            tenantId, saved.id(), command.assigneeType(),
                                                            command.assigneeId())))
                                                    .flatMap(assignment -> publish(resourceAssignedEvent(saved, assignment))
                                                            .thenReturn(saved)));
                                })
                                .switchIfEmpty(materialResourceRepository.save(resource.assign())
                                        .flatMap(saved -> resourceAssignmentRepository.save(ResourceAssignment.assign(
                                                        tenantId, saved.id(), command.assigneeType(),
                                                        command.assigneeId()))
                                                .flatMap(assignment -> publish(resourceAssignedEvent(saved, assignment))
                                                        .thenReturn(saved))))));
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<MaterialResource> unassign(UUID tenantId, UUID resourceId) {
        return transactionalExecutor.transactional(Mono.zip(getExistingResource(resourceId),
                        resourceAssignmentRepository.findActiveByTenantIdAndResourceId(tenantId, resourceId)
                                .switchIfEmpty(Mono.error(new ResourceAssignmentNotFoundException(resourceId))))
                .flatMap(tuple -> {
                    MaterialResource resource = tuple.getT1();
                    ResourceAssignment assignment = tuple.getT2().close();
                    return materialResourceRepository.save(resource.unassign())
                            .flatMap(saved -> resourceAssignmentRepository.save(assignment)
                                    .flatMap(closed -> publish(resourceUnassignedEvent(saved, closed))
                                            .thenReturn(saved)));
                }));
    }

    @Override
    public Mono<MaterialResource> dispose(UUID tenantId, UUID resourceId) {
        return transactionalExecutor.transactional(getExistingResource(resourceId)
                .flatMap(resource -> Mono.zip(
                                resourceAssignmentRepository.findActiveByTenantIdAndResourceId(tenantId, resourceId)
                                        .hasElement(),
                                resourceReservationRepository.findActiveByTenantIdAndResourceId(tenantId, resourceId)
                                        .hasElement())
                        .flatMap(tuple -> {
                            if (tuple.getT1()) {
                                return Mono.error(new ResourceConsistencyException(
                                        "Resource must be unassigned before disposal"));
                            }
                            if (tuple.getT2()) {
                                return Mono.error(new ResourceConsistencyException(
                                        "Resource must not have active reservation before disposal"));
                            }
                            return materialResourceRepository.save(resource.dispose())
                                    .flatMap(saved -> publish(resourceDisposedEvent(saved)).thenReturn(saved));
                        })));
    }

    @Override
    public Flux<ResourceAssignment> listAssignments(UUID tenantId, UUID resourceId) {
        return resourceAssignmentRepository.findByTenantIdAndResourceId(tenantId, resourceId);
    }

    @Override
    public Flux<ResourceAssignment> listAssignments(UUID tenantId, String assigneeType, UUID assigneeId) {
        return validateTargetExists(tenantId, assigneeType, assigneeId, false)
                .thenMany(resourceAssignmentRepository.findByTenantIdAndAssigneeTypeAndAssigneeId(tenantId,
                        normalizeTargetType(assigneeType, "assigneeType"), assigneeId));
    }

    @Override
    public Flux<ResourceReservation> listReservations(UUID tenantId, UUID resourceId) {
        return resourceReservationRepository.findByTenantIdAndResourceId(tenantId, resourceId);
    }

    @Override
    public Flux<ResourceReservation> listReservations(UUID tenantId, String reserveeType, UUID reserveeId) {
        return validateTargetExists(tenantId, reserveeType, reserveeId, false)
                .thenMany(resourceReservationRepository.findByTenantIdAndReserveeTypeAndReserveeId(tenantId,
                        normalizeTargetType(reserveeType, "reserveeType"), reserveeId));
    }

    @Override
    public Mono<MaterialResource> record(UUID tenantId, UUID resourceId, RecordMaintenanceCommand command) {
        Objects.requireNonNull(command, "command is required");
        return getExistingResource(resourceId)
                .map(resource -> toMaintenanceResource(resource, command.status()))
                .flatMap(materialResourceRepository::save)
                .flatMap(saved -> maintenanceRecordRepository.save(toMaintenanceRecord(tenantId, saved.id(), command))
                        .thenReturn(saved));
    }

    @Override
    public Flux<MaintenanceRecord> listMaintenance(UUID tenantId, UUID resourceId) {
        return maintenanceRecordRepository.findByTenantIdAndResourceId(tenantId, resourceId);
    }

    @Override
    public Mono<ResourceNetworkObservation> recordNetwork(UUID tenantId, UUID resourceId,
            RecordNetworkObservationCommand command) {
        Objects.requireNonNull(command, "command is required");
        return getExistingResource(resourceId)
                .then(resourceNetworkObservationRepository.save(ResourceNetworkObservation.record(tenantId, resourceId,
                        command.ipAddress(), command.macAddress())));
    }

    @Override
    public Flux<ResourceNetworkObservation> listNetworkObservations(UUID tenantId, UUID resourceId) {
        return resourceNetworkObservationRepository.findByTenantIdAndResourceId(tenantId, resourceId);
    }

    @Override
    public Mono<ResourceLocationObservation> recordLocation(UUID tenantId, UUID resourceId,
            RecordLocationObservationCommand command) {
        Objects.requireNonNull(command, "command is required");
        return getExistingResource(resourceId)
                .then(resourceLocationObservationRepository.save(ResourceLocationObservation.record(tenantId, resourceId,
                        command.latitude(), command.longitude())));
    }

    @Override
    public Flux<ResourceLocationObservation> listLocationObservations(UUID tenantId, UUID resourceId) {
        return resourceLocationObservationRepository.findByTenantIdAndResourceId(tenantId, resourceId);
    }

    private Mono<MaterialResource> getExistingResource(UUID resourceId) {
        return materialResourceRepository.findById(resourceId)
                .switchIfEmpty(Mono.error(new MaterialResourceNotFoundException(resourceId)));
    }

    private Mono<Void> validateResourceScope(UUID tenantId, UUID organizationId, UUID agencyId) {
        return organizationRepository.findById(tenantId, organizationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("organization not found")))
                .then(agencyId == null
                        ? Mono.empty()
                        : agencyRepository.findById(tenantId, agencyId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("agency not found")))
                        .flatMap(agency -> {
                            if (!agency.organizationId().equals(organizationId)) {
                                return Mono.error(new IllegalArgumentException("agency does not belong to the organization"));
                            }
                            if (!agency.active()) {
                                return Mono.error(new IllegalArgumentException("agency is not active"));
                            }
                            return Mono.empty();
                        }));
    }

    private Mono<Void> validateTargetScope(UUID tenantId, UUID organizationId, String targetType, UUID targetId,
            boolean requireActive) {
        return validateResourceScope(tenantId, organizationId, null)
                .then(validateTargetExists(tenantId, targetType, targetId, requireActive))
                .then(validateTargetOrganization(tenantId, organizationId, targetType, targetId, requireActive));
    }

    private Mono<Void> validateTargetExists(UUID tenantId, String targetType, UUID targetId, boolean requireActive) {
        String normalizedType = normalizeTargetType(targetType, "targetType");
        Objects.requireNonNull(targetId, "targetId is required");
        return switch (normalizedType) {
            case "ACTOR" -> actorRepository.findById(tenantId, targetId)
                    .switchIfEmpty(Mono.error(new IllegalArgumentException("actor not found")))
                    .flatMap(actor -> {
                        if (requireActive && actor.deletedAt() != null) {
                            return Mono.error(new IllegalArgumentException("actor is deleted"));
                        }
                        return Mono.empty();
                    });
            case "AGENCY" -> agencyRepository.findById(tenantId, targetId)
                    .switchIfEmpty(Mono.error(new IllegalArgumentException("agency not found")))
                    .flatMap(agency -> {
                        if (requireActive && !agency.active()) {
                            return Mono.error(new IllegalArgumentException("agency is not active"));
                        }
                        return Mono.empty();
                    });
            case "ORGANIZATION" -> organizationRepository.findById(tenantId, targetId)
                    .switchIfEmpty(Mono.error(new IllegalArgumentException("organization not found")))
                    .flatMap(organization -> {
                        if (requireActive && !organization.isActive()) {
                            return Mono.error(new IllegalArgumentException("organization is not active"));
                        }
                        return Mono.empty();
                    });
            case "PHYSICAL_SPACE" -> physicalSpaceRepository.findById(tenantId, targetId)
                    .switchIfEmpty(Mono.error(new IllegalArgumentException("physical space not found")))
                    .flatMap(space -> {
                        if (requireActive && !space.active()) {
                            return Mono.error(new IllegalArgumentException("physical space is not active"));
                        }
                        return Mono.empty();
                    });
            default -> Mono.error(new IllegalArgumentException("unsupported targetType: " + normalizedType));
        };
    }

    private Mono<Void> validateTargetOrganization(UUID tenantId, UUID organizationId, String targetType, UUID targetId,
            boolean requireActive) {
        String normalizedType = normalizeTargetType(targetType, "targetType");
        return switch (normalizedType) {
            case "ACTOR" -> actorRepository.findById(tenantId, targetId)
                    .flatMap(actor -> {
                        if (actor.organizationId() != null && !actor.organizationId().equals(organizationId)) {
                            return Mono.error(new IllegalArgumentException("actor does not belong to the organization"));
                        }
                        if (requireActive && actor.deletedAt() != null) {
                            return Mono.error(new IllegalArgumentException("actor is deleted"));
                        }
                        return Mono.empty();
                    });
            case "AGENCY" -> agencyRepository.findById(tenantId, targetId)
                    .flatMap(agency -> {
                        if (!agency.organizationId().equals(organizationId)) {
                            return Mono.error(new IllegalArgumentException("agency does not belong to the organization"));
                        }
                        if (requireActive && !agency.active()) {
                            return Mono.error(new IllegalArgumentException("agency is not active"));
                        }
                        return Mono.empty();
                    });
            case "ORGANIZATION" -> {
                if (!organizationId.equals(targetId)) {
                    yield Mono.error(new IllegalArgumentException("organization target must match resource organization"));
                }
                yield organizationRepository.findById(tenantId, targetId)
                        .flatMap(organization -> requireActive && !organization.isActive()
                                ? Mono.error(new IllegalArgumentException("organization is not active"))
                                : Mono.empty());
            }
            case "PHYSICAL_SPACE" -> physicalSpaceRepository.findById(tenantId, targetId)
                    .flatMap(space -> {
                        if (!space.organizationId().equals(organizationId)) {
                            return Mono.error(new IllegalArgumentException(
                                    "physical space does not belong to the organization"));
                        }
                        if (requireActive && !space.active()) {
                            return Mono.error(new IllegalArgumentException("physical space is not active"));
                        }
                        return Mono.empty();
                    });
            default -> Mono.error(new IllegalArgumentException("unsupported targetType: " + normalizedType));
        };
    }

    private String normalizeTargetType(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim().toUpperCase();
    }

    private boolean matchesQuery(MaterialResource resource, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalized = query.trim().toLowerCase();
        return contains(resource.resourceCode(), normalized)
                || contains(resource.name(), normalized)
                || contains(resource.category(), normalized)
                || contains(resource.serialNumber(), normalized)
                || contains(resource.ipAddress(), normalized)
                || contains(resource.macAddress(), normalized);
    }

    private boolean contains(String value, String normalizedQuery) {
        return value != null && value.toLowerCase().contains(normalizedQuery);
    }

    private MaterialResourceSearchResult toSearchResult(MaterialResource resource) {
        return new MaterialResourceSearchResult(
                resource.id(),
                resource.tenantId(),
                resource.organizationId(),
                resource.agencyId(),
                resource.resourceCode(),
                resource.name(),
                resource.category(),
                resource.serialNumber(),
                resource.status(),
                resource.latitude(),
                resource.longitude(),
                resource.ipAddress(),
                resource.macAddress());
    }

    private MaterialResource toMaintenanceResource(MaterialResource resource, String status) {
        String normalizedStatus = status == null || status.isBlank() ? "OPEN" : status.trim().toUpperCase();
        return "COMPLETED".equals(normalizedStatus) ? resource.markAvailable() : resource.markInMaintenance();
    }

    private MaintenanceRecord toMaintenanceRecord(UUID tenantId, UUID resourceId, RecordMaintenanceCommand command) {
        String normalizedStatus = command.status() == null || command.status().isBlank()
                ? "OPEN"
                : command.status().trim().toUpperCase();
        return "COMPLETED".equals(normalizedStatus)
                ? MaintenanceRecord.complete(tenantId, resourceId, command.maintenanceType(), command.description(),
                        Instant.now())
                : MaintenanceRecord.open(tenantId, resourceId, command.maintenanceType(), command.description());
    }

    private Mono<Void> publish(BusinessEvent event) {
        return businessEventPublisher.publish(event);
    }

    private BusinessEvent resourceReservedEvent(MaterialResource resource, ResourceReservation reservation) {
        return BusinessEvent.now(resource.tenantId(), resource.organizationId(), "RESOURCE_RESERVED", "MATERIAL_RESOURCE",
                resource.id(), payload(
                        "resourceCode", resource.resourceCode(),
                        "reservationId", reservation.id(),
                        "reserveeType", reservation.reserveeType(),
                        "reserveeId", reservation.reserveeId(),
                        "status", resource.status()));
    }

    private BusinessEvent resourceReservationReleasedEvent(MaterialResource resource, ResourceReservation reservation) {
        return BusinessEvent.now(resource.tenantId(), resource.organizationId(), "RESOURCE_RESERVATION_RELEASED",
                "MATERIAL_RESOURCE", resource.id(), payload(
                        "resourceCode", resource.resourceCode(),
                        "reservationId", reservation.id(),
                        "status", reservation.status(),
                        "resourceStatus", resource.status()));
    }

    private BusinessEvent resourceRegisteredEvent(MaterialResource resource) {
        return BusinessEvent.now(resource.tenantId(), resource.organizationId(), "MATERIAL_RESOURCE_REGISTERED",
                "MATERIAL_RESOURCE", resource.id(), payload(
                        "resourceCode", resource.resourceCode(),
                        "name", resource.name(),
                        "category", resource.category(),
                        "serialNumber", resource.serialNumber(),
                        "agencyId", resource.agencyId(),
                        "status", resource.status(),
                        "ipAddress", resource.ipAddress(),
                        "macAddress", resource.macAddress(),
                        "latitude", resource.latitude(),
                        "longitude", resource.longitude()));
    }

    private BusinessEvent resourceAssignedEvent(MaterialResource resource, ResourceAssignment assignment) {
        return BusinessEvent.now(resource.tenantId(), resource.organizationId(), "RESOURCE_ASSIGNED",
                "MATERIAL_RESOURCE", resource.id(), payload(
                        "resourceCode", resource.resourceCode(),
                        "assignmentId", assignment.id(),
                        "assigneeType", assignment.assigneeType(),
                        "assigneeId", assignment.assigneeId(),
                        "status", resource.status()));
    }

    private BusinessEvent resourceUnassignedEvent(MaterialResource resource, ResourceAssignment assignment) {
        return BusinessEvent.now(resource.tenantId(), resource.organizationId(), "RESOURCE_UNASSIGNED",
                "MATERIAL_RESOURCE", resource.id(), payload(
                        "resourceCode", resource.resourceCode(),
                        "assignmentId", assignment.id(),
                        "status", resource.status()));
    }

    private BusinessEvent resourceDisposedEvent(MaterialResource resource) {
        return BusinessEvent.now(resource.tenantId(), resource.organizationId(), "RESOURCE_DISPOSED",
                "MATERIAL_RESOURCE", resource.id(), payload(
                        "resourceCode", resource.resourceCode(),
                        "status", resource.status()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
