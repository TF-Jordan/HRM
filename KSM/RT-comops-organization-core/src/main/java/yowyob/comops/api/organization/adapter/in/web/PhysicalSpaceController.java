package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.organization.application.port.in.CreatePhysicalSpaceCommand;
import yowyob.comops.api.organization.application.port.in.CreatePhysicalSpaceUseCase;
import yowyob.comops.api.organization.application.port.in.ListPhysicalSpacesUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/organizations/{organizationId}/agencies/{agencyId}/physical-spaces")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
public class PhysicalSpaceController {

    private final CreatePhysicalSpaceUseCase createPhysicalSpaceUseCase;
    private final ListPhysicalSpacesUseCase listPhysicalSpacesUseCase;

    public PhysicalSpaceController(CreatePhysicalSpaceUseCase createPhysicalSpaceUseCase,
            ListPhysicalSpacesUseCase listPhysicalSpacesUseCase) {
        this.createPhysicalSpaceUseCase = createPhysicalSpaceUseCase;
        this.listPhysicalSpacesUseCase = listPhysicalSpacesUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<PhysicalSpaceResponse>>> create(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("agencyId") UUID agencyId,
            @Valid @RequestBody Mono<CreatePhysicalSpaceRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> createPhysicalSpaceUseCase.create(new CreatePhysicalSpaceCommand(
                        tuple.getT2().tenantId(),
                        organizationId,
                        agencyId,
                        tuple.getT1().parentSpaceId(),
                        tuple.getT1().code(),
                        tuple.getT1().name(),
                        tuple.getT1().spaceType(),
                        tuple.getT1().description(),
                        tuple.getT1().levelNumber(),
                        tuple.getT1().capacity(),
                        tuple.getT1().active() == null || tuple.getT1().active())))
                .map(PhysicalSpaceResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Physical space created.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<PhysicalSpaceResponse>>>> list(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("agencyId") UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listPhysicalSpacesUseCase.listByAgency(context.tenantId(), organizationId, agencyId)
                        .map(PhysicalSpaceResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Physical spaces fetched.")));
    }

    @GetMapping("/tree")
    public Mono<ResponseEntity<ApiResponse<List<PhysicalSpaceResponse>>>> tree(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("agencyId") UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listPhysicalSpacesUseCase.listByAgency(context.tenantId(), organizationId, agencyId)
                        .collectList()
                        .map(PhysicalSpaceResponse::tree))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Physical space tree fetched.")));
    }
}
