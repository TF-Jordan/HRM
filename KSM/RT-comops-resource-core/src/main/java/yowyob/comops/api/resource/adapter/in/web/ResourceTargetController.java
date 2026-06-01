package yowyob.comops.api.resource.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.resource.application.port.in.ListResourceAssignmentsUseCase;
import yowyob.comops.api.resource.application.port.in.ListResourceReservationsUseCase;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'resources:write')")
public class ResourceTargetController {

    private final ListResourceAssignmentsUseCase listResourceAssignmentsUseCase;
    private final ListResourceReservationsUseCase listResourceReservationsUseCase;

    public ResourceTargetController(ListResourceAssignmentsUseCase listResourceAssignmentsUseCase,
            ListResourceReservationsUseCase listResourceReservationsUseCase) {
        this.listResourceAssignmentsUseCase = listResourceAssignmentsUseCase;
        this.listResourceReservationsUseCase = listResourceReservationsUseCase;
    }

    @GetMapping("/api/actors/{actorId}/resources/assignments")
    public Mono<ResponseEntity<ApiResponse<List<ResourceAssignmentResponse>>>> listActorAssignments(
            @PathVariable("actorId") UUID actorId) {
        return listAssignments("ACTOR", actorId, "Actor assignments fetched.");
    }

    @GetMapping("/api/actors/{actorId}/resources/reservations")
    public Mono<ResponseEntity<ApiResponse<List<ResourceReservationResponse>>>> listActorReservations(
            @PathVariable("actorId") UUID actorId) {
        return listReservations("ACTOR", actorId, "Actor reservations fetched.");
    }

    @GetMapping("/api/agencies/{agencyId}/resources/assignments")
    public Mono<ResponseEntity<ApiResponse<List<ResourceAssignmentResponse>>>> listAgencyAssignments(
            @PathVariable("agencyId") UUID agencyId) {
        return listAssignments("AGENCY", agencyId, "Agency assignments fetched.");
    }

    @GetMapping("/api/agencies/{agencyId}/resources/reservations")
    public Mono<ResponseEntity<ApiResponse<List<ResourceReservationResponse>>>> listAgencyReservations(
            @PathVariable("agencyId") UUID agencyId) {
        return listReservations("AGENCY", agencyId, "Agency reservations fetched.");
    }

    @GetMapping("/api/organizations/{organizationId}/resources/assignments")
    public Mono<ResponseEntity<ApiResponse<List<ResourceAssignmentResponse>>>> listOrganizationAssignments(
            @PathVariable("organizationId") UUID organizationId) {
        return listAssignments("ORGANIZATION", organizationId, "Organization assignments fetched.");
    }

    @GetMapping("/api/organizations/{organizationId}/resources/reservations")
    public Mono<ResponseEntity<ApiResponse<List<ResourceReservationResponse>>>> listOrganizationReservations(
            @PathVariable("organizationId") UUID organizationId) {
        return listReservations("ORGANIZATION", organizationId, "Organization reservations fetched.");
    }

    @GetMapping("/api/physical-spaces/{spaceId}/resources/assignments")
    public Mono<ResponseEntity<ApiResponse<List<ResourceAssignmentResponse>>>> listPhysicalSpaceAssignments(
            @PathVariable("spaceId") UUID spaceId) {
        return listAssignments("PHYSICAL_SPACE", spaceId, "Physical space assignments fetched.");
    }

    @GetMapping("/api/physical-spaces/{spaceId}/resources/reservations")
    public Mono<ResponseEntity<ApiResponse<List<ResourceReservationResponse>>>> listPhysicalSpaceReservations(
            @PathVariable("spaceId") UUID spaceId) {
        return listReservations("PHYSICAL_SPACE", spaceId, "Physical space reservations fetched.");
    }

    private Mono<ResponseEntity<ApiResponse<List<ResourceAssignmentResponse>>>> listAssignments(String assigneeType,
            UUID assigneeId, String message) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listResourceAssignmentsUseCase
                        .listAssignments(context.tenantId(), assigneeType, assigneeId)
                        .map(ResourceAssignmentResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, message)));
    }

    private Mono<ResponseEntity<ApiResponse<List<ResourceReservationResponse>>>> listReservations(String reserveeType,
            UUID reserveeId, String message) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listResourceReservationsUseCase
                        .listReservations(context.tenantId(), reserveeType, reserveeId)
                        .map(ResourceReservationResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, message)));
    }
}
