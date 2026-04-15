package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.organization.application.port.in.AddOpeningHoursExceptionCommand;
import yowyob.comops.api.organization.application.port.in.AddOpeningHoursExceptionUseCase;
import yowyob.comops.api.organization.application.port.in.GetAgencyOpenStatusUseCase;
import yowyob.comops.api.organization.application.port.in.GetAgencyScheduleUseCase;
import yowyob.comops.api.organization.application.port.in.RemoveOpeningHoursExceptionUseCase;
import yowyob.comops.api.organization.application.port.in.ReplaceRegularOpeningHoursUseCase;
import yowyob.comops.api.organization.application.port.in.UpsertOpeningHoursCommand;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/agencies/{agencyId}/schedule")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
public class AgencyScheduleController {

    private final GetAgencyScheduleUseCase getAgencyScheduleUseCase;
    private final ReplaceRegularOpeningHoursUseCase replaceRegularOpeningHoursUseCase;
    private final AddOpeningHoursExceptionUseCase addOpeningHoursExceptionUseCase;
    private final RemoveOpeningHoursExceptionUseCase removeOpeningHoursExceptionUseCase;
    private final GetAgencyOpenStatusUseCase getAgencyOpenStatusUseCase;

    public AgencyScheduleController(GetAgencyScheduleUseCase getAgencyScheduleUseCase,
            ReplaceRegularOpeningHoursUseCase replaceRegularOpeningHoursUseCase,
            AddOpeningHoursExceptionUseCase addOpeningHoursExceptionUseCase,
            RemoveOpeningHoursExceptionUseCase removeOpeningHoursExceptionUseCase,
            GetAgencyOpenStatusUseCase getAgencyOpenStatusUseCase) {
        this.getAgencyScheduleUseCase = getAgencyScheduleUseCase;
        this.replaceRegularOpeningHoursUseCase = replaceRegularOpeningHoursUseCase;
        this.addOpeningHoursExceptionUseCase = addOpeningHoursExceptionUseCase;
        this.removeOpeningHoursExceptionUseCase = removeOpeningHoursExceptionUseCase;
        this.getAgencyOpenStatusUseCase = getAgencyOpenStatusUseCase;
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<AgencyScheduleResponse>>> getSchedule(@PathVariable UUID agencyId,
            @RequestParam("organizationId") UUID organizationId) {
        return getAgencyScheduleUseCase.getSchedule(organizationId, agencyId)
                .map(AgencyScheduleResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency schedule retrieved.")));
    }

    @PutMapping("/regular")
    public Mono<ResponseEntity<ApiResponse<List<OpeningHoursResponse>>>> replaceRegularSchedule(@PathVariable UUID agencyId,
            @RequestParam("organizationId") UUID organizationId,
            @Valid @RequestBody Mono<RegularOpeningHoursRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> replaceRegularOpeningHoursUseCase.replaceRegularHours(tuple.getT2().tenantId(),
                        organizationId, agencyId, tuple.getT1().rules().stream()
                                .map(rule -> new UpsertOpeningHoursCommand(tuple.getT2().tenantId(), organizationId,
                                        agencyId, rule.dayOfWeek(), rule.opensAt(), rule.closesAt(), rule.closed()))
                                .toList())
                        .map(OpeningHoursResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Regular schedule replaced.")));
    }

    @PostMapping("/exceptions")
    public Mono<ResponseEntity<ApiResponse<OpeningHoursExceptionResponse>>> addException(@PathVariable UUID agencyId,
            @RequestParam("organizationId") UUID organizationId,
            @Valid @RequestBody Mono<OpeningHoursExceptionRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> addOpeningHoursExceptionUseCase.addException(new AddOpeningHoursExceptionCommand(
                        tuple.getT2().tenantId(), organizationId, agencyId, tuple.getT1().exceptionDate(),
                        tuple.getT1().label(), tuple.getT1().opensAt(), tuple.getT1().closesAt(), tuple.getT1().closed())))
                .map(OpeningHoursExceptionResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Opening hours exception created.")));
    }

    @DeleteMapping("/exceptions/{exceptionId}")
    public Mono<ResponseEntity<ApiResponse<Void>>> removeException(@PathVariable UUID exceptionId) {
        return removeOpeningHoursExceptionUseCase.removeException(exceptionId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Opening hours exception removed.")));
    }

    @GetMapping("/status")
    public Mono<ResponseEntity<ApiResponse<AgencyOpenStatusResponse>>> getStatus(@PathVariable UUID agencyId,
            @RequestParam("organizationId") UUID organizationId,
            @RequestParam(value = "at", required = false) LocalDateTime at) {
        return getAgencyOpenStatusUseCase.isOpen(organizationId, agencyId, at)
                .map(AgencyOpenStatusResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency open status retrieved.")));
    }
}
