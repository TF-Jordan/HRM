package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.organization.application.port.in.ListOpeningHoursUseCase;
import yowyob.comops.api.organization.application.port.in.UpsertOpeningHoursCommand;
import yowyob.comops.api.organization.application.port.in.UpsertOpeningHoursUseCase;
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
@RequestMapping("/api/organizations/opening-hours")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
public class OpeningHoursController {

    private final UpsertOpeningHoursUseCase useCase;
    private final ListOpeningHoursUseCase listOpeningHoursUseCase;

    public OpeningHoursController(UpsertOpeningHoursUseCase useCase, ListOpeningHoursUseCase listOpeningHoursUseCase) {
        this.useCase = useCase;
        this.listOpeningHoursUseCase = listOpeningHoursUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<OpeningHoursResponse>>> upsert(@Valid @RequestBody Mono<UpsertOpeningHoursRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.upsert(new UpsertOpeningHoursCommand(tuple.getT2().tenantId(), tuple.getT1().organizationId(),
                        tuple.getT1().agencyId(), tuple.getT1().dayOfWeek(), tuple.getT1().opensAt(), tuple.getT1().closesAt(),
                        tuple.getT1().closed())))
                .map(OpeningHoursResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Opening hours saved.")));
    }

    @GetMapping("/{organizationId}/agencies/{agencyId}")
    public Mono<ResponseEntity<ApiResponse<List<OpeningHoursResponse>>>> listByAgency(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("agencyId") UUID agencyId) {
        return listOpeningHoursUseCase.listByAgency(organizationId, agencyId)
                .sort((left, right) -> left.dayOfWeek().compareTo(right.dayOfWeek()))
                .map(OpeningHoursResponse::from)
                .collectList()
                .map(response -> ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.success(response, "Opening hours retrieved.")));
    }
}
