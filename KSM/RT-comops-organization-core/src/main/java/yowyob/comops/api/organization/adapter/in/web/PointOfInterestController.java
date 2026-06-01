package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.organization.application.port.in.CreatePointOfInterestCommand;
import yowyob.comops.api.organization.application.port.in.CreatePointOfInterestUseCase;
import yowyob.comops.api.organization.application.port.in.ListPointsOfInterestUseCase;
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
@RequestMapping("/api/organizations/points-of-interest")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
public class PointOfInterestController {

    private final CreatePointOfInterestUseCase useCase;
    private final ListPointsOfInterestUseCase listPointsOfInterestUseCase;

    public PointOfInterestController(CreatePointOfInterestUseCase useCase,
            ListPointsOfInterestUseCase listPointsOfInterestUseCase) {
        this.useCase = useCase;
        this.listPointsOfInterestUseCase = listPointsOfInterestUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<PointOfInterestResponse>>> create(@Valid @RequestBody Mono<CreatePointOfInterestRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.create(new CreatePointOfInterestCommand(tuple.getT2().tenantId(), tuple.getT1().organizationId(),
                        tuple.getT1().agencyId(), tuple.getT1().name(), tuple.getT1().poiType(), tuple.getT1().latitude(), tuple.getT1().longitude())))
                .map(PointOfInterestResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Point of interest created.")));
    }

    @GetMapping("/{organizationId}/agencies/{agencyId}")
    public Mono<ResponseEntity<ApiResponse<List<PointOfInterestResponse>>>> listByAgency(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("agencyId") UUID agencyId) {
        return listPointsOfInterestUseCase.listByAgency(organizationId, agencyId)
                .map(PointOfInterestResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Points of interest retrieved.")));
    }
}
