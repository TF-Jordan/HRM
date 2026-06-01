package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.organization.application.port.in.CreatePointOfInterestCommand;
import yowyob.comops.api.organization.application.port.in.CreatePointOfInterestUseCase;
import yowyob.comops.api.organization.application.port.in.LinkAgencyToPointOfInterestUseCase;
import yowyob.comops.api.organization.application.port.in.ListAllPointsOfInterestUseCase;
import yowyob.comops.api.organization.application.port.in.UnlinkAgencyFromPointOfInterestUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/pois")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
public class LegacyPointOfInterestController {

    private final CreatePointOfInterestUseCase createPointOfInterestUseCase;
    private final ListAllPointsOfInterestUseCase listAllPointsOfInterestUseCase;
    private final LinkAgencyToPointOfInterestUseCase linkAgencyToPointOfInterestUseCase;
    private final UnlinkAgencyFromPointOfInterestUseCase unlinkAgencyFromPointOfInterestUseCase;

    public LegacyPointOfInterestController(CreatePointOfInterestUseCase createPointOfInterestUseCase,
            ListAllPointsOfInterestUseCase listAllPointsOfInterestUseCase,
            LinkAgencyToPointOfInterestUseCase linkAgencyToPointOfInterestUseCase,
            UnlinkAgencyFromPointOfInterestUseCase unlinkAgencyFromPointOfInterestUseCase) {
        this.createPointOfInterestUseCase = createPointOfInterestUseCase;
        this.listAllPointsOfInterestUseCase = listAllPointsOfInterestUseCase;
        this.linkAgencyToPointOfInterestUseCase = linkAgencyToPointOfInterestUseCase;
        this.unlinkAgencyFromPointOfInterestUseCase = unlinkAgencyFromPointOfInterestUseCase;
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<PointOfInterestResponse>>>> getAll(@RequestParam("organizationId") UUID organizationId) {
        return listAllPointsOfInterestUseCase.listAll(organizationId)
                .map(PointOfInterestResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Points of interest retrieved.")));
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<PointOfInterestResponse>>> create(@Valid @RequestBody Mono<CreatePointOfInterestRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> createPointOfInterestUseCase.create(new CreatePointOfInterestCommand(tuple.getT2().tenantId(),
                        tuple.getT1().organizationId(), tuple.getT1().agencyId(), tuple.getT1().name(),
                        tuple.getT1().poiType(), tuple.getT1().latitude(), tuple.getT1().longitude())))
                .map(PointOfInterestResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Point of interest created.")));
    }

    @PostMapping("/link")
    public Mono<ResponseEntity<ApiResponse<Void>>> link(@Valid @RequestBody Mono<LinkPointOfInterestRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> linkAgencyToPointOfInterestUseCase.link(tuple.getT2().tenantId(), tuple.getT1().organizationId(),
                        tuple.getT1().agencyId(), tuple.getT1().poiId(), tuple.getT1().distanceMeters(),
                        tuple.getT1().description()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null, "Point of interest linked.")));
    }

    @DeleteMapping("/link")
    public Mono<ResponseEntity<ApiResponse<Void>>> unlink(@RequestParam UUID agencyId, @RequestParam UUID poiId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> unlinkAgencyFromPointOfInterestUseCase.unlink(context.tenantId(), agencyId, poiId))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Point of interest unlinked.")));
    }
}
