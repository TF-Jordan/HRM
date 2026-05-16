package yowyob.comops.api.resource.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
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
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/resources")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'resources:write')")
public class ResourceController {

    private final RegisterMaterialResourceUseCase registerMaterialResourceUseCase;
    private final GetMaterialResourceUseCase getMaterialResourceUseCase;
    private final ListMaterialResourcesUseCase listMaterialResourcesUseCase;
    private final ReserveMaterialResourceUseCase reserveMaterialResourceUseCase;
    private final ReleaseResourceReservationUseCase releaseResourceReservationUseCase;
    private final AssignMaterialResourceUseCase assignMaterialResourceUseCase;
    private final UnassignMaterialResourceUseCase unassignMaterialResourceUseCase;
    private final DisposeMaterialResourceUseCase disposeMaterialResourceUseCase;
    private final ListResourceAssignmentsUseCase listResourceAssignmentsUseCase;
    private final ListResourceReservationsUseCase listResourceReservationsUseCase;
    private final RecordMaintenanceUseCase recordMaintenanceUseCase;
    private final ListMaintenanceRecordsUseCase listMaintenanceRecordsUseCase;
    private final RecordNetworkObservationUseCase recordNetworkObservationUseCase;
    private final ListNetworkObservationsUseCase listNetworkObservationsUseCase;
    private final RecordLocationObservationUseCase recordLocationObservationUseCase;
    private final ListLocationObservationsUseCase listLocationObservationsUseCase;
    private final SearchMaterialResourcesUseCase searchMaterialResourcesUseCase;

    public ResourceController(RegisterMaterialResourceUseCase registerMaterialResourceUseCase,
            GetMaterialResourceUseCase getMaterialResourceUseCase,
            ListMaterialResourcesUseCase listMaterialResourcesUseCase,
            ReserveMaterialResourceUseCase reserveMaterialResourceUseCase,
            ReleaseResourceReservationUseCase releaseResourceReservationUseCase,
            AssignMaterialResourceUseCase assignMaterialResourceUseCase,
            UnassignMaterialResourceUseCase unassignMaterialResourceUseCase,
            DisposeMaterialResourceUseCase disposeMaterialResourceUseCase,
            ListResourceAssignmentsUseCase listResourceAssignmentsUseCase,
            ListResourceReservationsUseCase listResourceReservationsUseCase,
            RecordMaintenanceUseCase recordMaintenanceUseCase,
            ListMaintenanceRecordsUseCase listMaintenanceRecordsUseCase,
            RecordNetworkObservationUseCase recordNetworkObservationUseCase,
            ListNetworkObservationsUseCase listNetworkObservationsUseCase,
            RecordLocationObservationUseCase recordLocationObservationUseCase,
            ListLocationObservationsUseCase listLocationObservationsUseCase,
            SearchMaterialResourcesUseCase searchMaterialResourcesUseCase) {
        this.registerMaterialResourceUseCase = registerMaterialResourceUseCase;
        this.getMaterialResourceUseCase = getMaterialResourceUseCase;
        this.listMaterialResourcesUseCase = listMaterialResourcesUseCase;
        this.reserveMaterialResourceUseCase = reserveMaterialResourceUseCase;
        this.releaseResourceReservationUseCase = releaseResourceReservationUseCase;
        this.assignMaterialResourceUseCase = assignMaterialResourceUseCase;
        this.unassignMaterialResourceUseCase = unassignMaterialResourceUseCase;
        this.disposeMaterialResourceUseCase = disposeMaterialResourceUseCase;
        this.listResourceAssignmentsUseCase = listResourceAssignmentsUseCase;
        this.listResourceReservationsUseCase = listResourceReservationsUseCase;
        this.recordMaintenanceUseCase = recordMaintenanceUseCase;
        this.listMaintenanceRecordsUseCase = listMaintenanceRecordsUseCase;
        this.recordNetworkObservationUseCase = recordNetworkObservationUseCase;
        this.listNetworkObservationsUseCase = listNetworkObservationsUseCase;
        this.recordLocationObservationUseCase = recordLocationObservationUseCase;
        this.listLocationObservationsUseCase = listLocationObservationsUseCase;
        this.searchMaterialResourcesUseCase = searchMaterialResourcesUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<MaterialResourceResponse>>> registerResource(
            @Valid @RequestBody Mono<RegisterMaterialResourceRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> registerMaterialResourceUseCase.register(new RegisterMaterialResourceCommand(
                        tuple.getT2().tenantId(), tuple.getT1().organizationId(), tuple.getT1().agencyId(),
                        tuple.getT1().resourceCode(), tuple.getT1().name(), tuple.getT1().category(),
                        tuple.getT1().serialNumber(), tuple.getT1().latitude(), tuple.getT1().longitude(),
                        tuple.getT1().ipAddress(), tuple.getT1().macAddress())))
                .map(MaterialResourceResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Resource registered.")));
    }

    @GetMapping("/{resourceId}")
    public Mono<ResponseEntity<ApiResponse<MaterialResourceResponse>>> getResource(
            @PathVariable("resourceId") UUID resourceId) {
        return getMaterialResourceUseCase.getResource(resourceId)
                .map(MaterialResourceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Resource fetched.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<MaterialResourceResponse>>>> listResources(
            @RequestParam("organizationId") UUID organizationId,
            @RequestParam(name = "agencyId", required = false) UUID agencyId,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "status", required = false) String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listMaterialResourcesUseCase.listResources(context.tenantId(), organizationId,
                        agencyId, category, status)
                        .map(MaterialResourceResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Resources fetched.")));
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<ApiResponse<List<MaterialResourceSearchResponse>>>> searchResources(
            @RequestParam("organizationId") UUID organizationId,
            @RequestParam("q") String query,
            @RequestParam(name = "agencyId", required = false) UUID agencyId,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "status", required = false) String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> searchMaterialResourcesUseCase.searchResources(context.tenantId(), organizationId,
                                agencyId, query, category, status)
                        .map(MaterialResourceSearchResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                        "Resource search results retrieved.")));
    }

    @PostMapping("/{resourceId}/reservations")
    @PreAuthorize("@businessAccessPolicy.canReserveResource(authentication)")
    public Mono<ResponseEntity<ApiResponse<MaterialResourceResponse>>> reserveResource(
            @PathVariable("resourceId") UUID resourceId,
            @Valid @RequestBody Mono<ReserveMaterialResourceRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> reserveMaterialResourceUseCase.reserve(tuple.getT2().tenantId(),
                        new ReserveMaterialResourceCommand(resourceId, tuple.getT1().reserveeType(),
                                tuple.getT1().reserveeId(), tuple.getT1().reason())))
                .map(MaterialResourceResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Resource reserved.")));
    }

    @GetMapping("/{resourceId}/reservations")
    public Mono<ResponseEntity<ApiResponse<List<ResourceReservationResponse>>>> listReservations(
            @PathVariable("resourceId") UUID resourceId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listResourceReservationsUseCase.listReservations(context.tenantId(), resourceId)
                        .map(ResourceReservationResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Reservations fetched.")));
    }

    @PostMapping("/{resourceId}/reservations/{reservationId}/release")
    @PreAuthorize("@businessAccessPolicy.canReserveResource(authentication)")
    public Mono<ResponseEntity<ApiResponse<MaterialResourceResponse>>> releaseReservation(
            @PathVariable("resourceId") UUID resourceId,
            @PathVariable("reservationId") UUID reservationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> releaseResourceReservationUseCase.releaseReservation(context.tenantId(), resourceId,
                        reservationId))
                .map(MaterialResourceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Reservation released.")));
    }

    @PostMapping("/{resourceId}/assignments")
    public Mono<ResponseEntity<ApiResponse<MaterialResourceResponse>>> assignResource(
            @PathVariable("resourceId") UUID resourceId,
            @Valid @RequestBody Mono<AssignMaterialResourceRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> assignMaterialResourceUseCase.assign(tuple.getT2().tenantId(),
                        new AssignMaterialResourceCommand(resourceId, tuple.getT1().assigneeType(),
                                tuple.getT1().assigneeId())))
                .map(MaterialResourceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Resource assigned.")));
    }

    @PostMapping("/{resourceId}/unassign")
    @PreAuthorize("@businessAccessPolicy.canUnassignResource(authentication)")
    public Mono<ResponseEntity<ApiResponse<MaterialResourceResponse>>> unassignResource(
            @PathVariable("resourceId") UUID resourceId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> unassignMaterialResourceUseCase.unassign(context.tenantId(), resourceId))
                .map(MaterialResourceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Resource unassigned.")));
    }

    @GetMapping("/{resourceId}/assignments")
    public Mono<ResponseEntity<ApiResponse<List<ResourceAssignmentResponse>>>> listAssignments(
            @PathVariable("resourceId") UUID resourceId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listResourceAssignmentsUseCase.listAssignments(context.tenantId(), resourceId)
                        .map(ResourceAssignmentResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Assignments fetched.")));
    }

    @PostMapping("/{resourceId}/maintenance")
    public Mono<ResponseEntity<ApiResponse<MaterialResourceResponse>>> recordMaintenance(
            @PathVariable("resourceId") UUID resourceId,
            @Valid @RequestBody Mono<RecordMaintenanceRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> recordMaintenanceUseCase.record(tuple.getT2().tenantId(), resourceId,
                        new RecordMaintenanceCommand(tuple.getT1().maintenanceType(), tuple.getT1().description(),
                                tuple.getT1().status())))
                .map(MaterialResourceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Maintenance recorded.")));
    }

    @GetMapping("/{resourceId}/maintenance")
    public Mono<ResponseEntity<ApiResponse<List<MaintenanceRecordResponse>>>> listMaintenance(
            @PathVariable("resourceId") UUID resourceId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listMaintenanceRecordsUseCase.listMaintenance(context.tenantId(), resourceId)
                        .map(MaintenanceRecordResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Maintenance records fetched.")));
    }

    @PostMapping("/{resourceId}/network-observations")
    public Mono<ResponseEntity<ApiResponse<ResourceNetworkObservationResponse>>> recordNetworkObservation(
            @PathVariable("resourceId") UUID resourceId,
            @Valid @RequestBody Mono<RecordNetworkObservationRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> recordNetworkObservationUseCase.recordNetwork(tuple.getT2().tenantId(), resourceId,
                        new RecordNetworkObservationCommand(tuple.getT1().ipAddress(), tuple.getT1().macAddress())))
                .map(ResourceNetworkObservationResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Network observation recorded.")));
    }

    @GetMapping("/{resourceId}/network-observations")
    public Mono<ResponseEntity<ApiResponse<List<ResourceNetworkObservationResponse>>>> listNetworkObservations(
            @PathVariable("resourceId") UUID resourceId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listNetworkObservationsUseCase.listNetworkObservations(context.tenantId(),
                        resourceId)
                        .map(ResourceNetworkObservationResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Network observations fetched.")));
    }

    @PostMapping("/{resourceId}/location-observations")
    public Mono<ResponseEntity<ApiResponse<ResourceLocationObservationResponse>>> recordLocationObservation(
            @PathVariable("resourceId") UUID resourceId,
            @Valid @RequestBody Mono<RecordLocationObservationRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> recordLocationObservationUseCase.recordLocation(tuple.getT2().tenantId(), resourceId,
                        new RecordLocationObservationCommand(tuple.getT1().latitude(), tuple.getT1().longitude())))
                .map(ResourceLocationObservationResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Location observation recorded.")));
    }

    @GetMapping("/{resourceId}/location-observations")
    public Mono<ResponseEntity<ApiResponse<List<ResourceLocationObservationResponse>>>> listLocationObservations(
            @PathVariable("resourceId") UUID resourceId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listLocationObservationsUseCase.listLocationObservations(context.tenantId(),
                        resourceId)
                        .map(ResourceLocationObservationResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Location observations fetched.")));
    }

    @PostMapping("/{resourceId}/dispose")
    @PreAuthorize("@businessAccessPolicy.canDisposeResource(authentication)")
    public Mono<ResponseEntity<ApiResponse<MaterialResourceResponse>>> disposeResource(
            @PathVariable("resourceId") UUID resourceId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> disposeMaterialResourceUseCase.dispose(context.tenantId(), resourceId))
                .map(MaterialResourceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Resource disposed.")));
    }
}
