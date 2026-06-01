package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.organization.application.port.in.ActivateAgencyUseCase;
import yowyob.comops.api.organization.application.port.in.CreateAgencyCommand;
import yowyob.comops.api.organization.application.port.in.CreateAgencyUseCase;
import yowyob.comops.api.organization.application.port.in.CloseAgencyUseCase;
import yowyob.comops.api.organization.application.port.in.ListAgenciesUseCase;
import yowyob.comops.api.organization.application.port.in.SuspendAgencyUseCase;
import yowyob.comops.api.organization.application.port.in.UpdateAgencyCommand;
import yowyob.comops.api.organization.application.port.in.UpdateAgencyUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@RestController
@RequestMapping("/api/organizations/{organizationId}/agencies")
public class AgencyController {

    private final CreateAgencyUseCase createAgencyUseCase;
    private final ListAgenciesUseCase listAgenciesUseCase;
    private final UpdateAgencyUseCase updateAgencyUseCase;
    private final ActivateAgencyUseCase activateAgencyUseCase;
    private final SuspendAgencyUseCase suspendAgencyUseCase;
    private final CloseAgencyUseCase closeAgencyUseCase;

    public AgencyController(CreateAgencyUseCase createAgencyUseCase, ListAgenciesUseCase listAgenciesUseCase,
            UpdateAgencyUseCase updateAgencyUseCase, ActivateAgencyUseCase activateAgencyUseCase,
            SuspendAgencyUseCase suspendAgencyUseCase,
            CloseAgencyUseCase closeAgencyUseCase) {
        this.createAgencyUseCase = createAgencyUseCase;
        this.listAgenciesUseCase = listAgenciesUseCase;
        this.updateAgencyUseCase = updateAgencyUseCase;
        this.activateAgencyUseCase = activateAgencyUseCase;
        this.suspendAgencyUseCase = suspendAgencyUseCase;
        this.closeAgencyUseCase = closeAgencyUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
    public Mono<ResponseEntity<ApiResponse<AgencyResponse>>> createAgency(
            @PathVariable("organizationId") UUID organizationId,
            @Valid @RequestBody Mono<CreateAgencyRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> createAgencyUseCase.createAgency(new CreateAgencyCommand(tuple.getT2().tenantId(),
                        organizationId, tuple.getT1().code(), tuple.getT1().ownerId(), tuple.getT1().managerId(),
                        tuple.getT1().name(), tuple.getT1().location(), tuple.getT1().description(),
                        tuple.getT1().resolvedTransferable(), tuple.getT1().resolvedActive(), tuple.getT1().logoUri(),
                        tuple.getT1().logoId(), tuple.getT1().shortName(), tuple.getT1().longName(),
                        tuple.getT1().resolvedIndividualBusiness(), tuple.getT1().resolvedHeadquarter(),
                        tuple.getT1().country(), tuple.getT1().city(), tuple.getT1().latitude(),
                        tuple.getT1().longitude(), tuple.getT1().openTime(), tuple.getT1().closeTime(),
                        tuple.getT1().phone(), tuple.getT1().email(), tuple.getT1().whatsapp(),
                        tuple.getT1().greetingMessage(), tuple.getT1().averageRevenue(), tuple.getT1().capitalShare(),
                        tuple.getT1().registrationNumber(), tuple.getT1().socialNetwork(), tuple.getT1().taxNumber(),
                        tuple.getT1().keywords(), tuple.getT1().resolvedPublic(), tuple.getT1().resolvedBusiness(),
                        tuple.getT1().totalAffiliatedCustomers(), tuple.getT1().agencyType())))
                .map(AgencyResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Agency created.")));
    }

    @PatchMapping("/{agencyId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
    public Mono<ResponseEntity<ApiResponse<AgencyResponse>>> updateAgency(
            @PathVariable("agencyId") UUID agencyId,
            @Valid @RequestBody Mono<UpdateAgencyRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> updateAgencyUseCase.updateAgency(new UpdateAgencyCommand(tuple.getT2().tenantId(),
                        agencyId, tuple.getT1().code(), tuple.getT1().ownerId(), tuple.getT1().managerId(),
                        tuple.getT1().name(), tuple.getT1().location(), tuple.getT1().description(),
                        tuple.getT1().resolvedTransferable(), tuple.getT1().resolvedActive(), tuple.getT1().logoUri(),
                        tuple.getT1().logoId(), tuple.getT1().shortName(), tuple.getT1().longName(),
                        tuple.getT1().resolvedIndividualBusiness(), tuple.getT1().resolvedHeadquarter(),
                        tuple.getT1().country(), tuple.getT1().city(), tuple.getT1().latitude(),
                        tuple.getT1().longitude(), tuple.getT1().openTime(), tuple.getT1().closeTime(),
                        tuple.getT1().phone(), tuple.getT1().email(), tuple.getT1().whatsapp(),
                        tuple.getT1().greetingMessage(), tuple.getT1().averageRevenue(), tuple.getT1().capitalShare(),
                        tuple.getT1().registrationNumber(), tuple.getT1().socialNetwork(), tuple.getT1().taxNumber(),
                        tuple.getT1().keywords(), tuple.getT1().resolvedPublic(), tuple.getT1().resolvedBusiness(),
                        tuple.getT1().totalAffiliatedCustomers(), tuple.getT1().agencyType())))
                .map(AgencyResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency updated.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
    public Mono<ResponseEntity<ApiResponse<List<AgencyResponse>>>> listAgencies(
            @PathVariable("organizationId") UUID organizationId) {
        return listAgenciesUseCase.listAgencies(organizationId)
                .map(AgencyResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agencies retrieved.")));
    }

    @PostMapping("/{agencyId}/activate")
    @PreAuthorize("@businessAccessPolicy.canGovernAgencies(authentication)")
    public Mono<ResponseEntity<ApiResponse<AgencyResponse>>> activateAgency(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("agencyId") UUID agencyId,
            @RequestBody Mono<GovernanceActionRequest> requestMono) {
        return requestMono.defaultIfEmpty(new GovernanceActionRequest(null))
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> activateAgencyUseCase
                        .activateAgency(tuple.getT2().tenantId(), organizationId, agencyId, tuple.getT2().userId(),
                                tuple.getT1().reason())
                        .then(listAgenciesUseCase.listAgencies(organizationId)
                                .filter(agency -> agency.id().equals(agencyId))
                                .singleOrEmpty()))
                .map(AgencyResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency activated.")));
    }

    @PostMapping("/{agencyId}/suspend")
    @PreAuthorize("@businessAccessPolicy.canGovernAgencies(authentication)")
    public Mono<ResponseEntity<ApiResponse<AgencyResponse>>> suspendAgency(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("agencyId") UUID agencyId,
            @RequestBody Mono<GovernanceActionRequest> requestMono) {
        return requestMono.defaultIfEmpty(new GovernanceActionRequest(null))
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> suspendAgencyUseCase
                        .suspendAgency(tuple.getT2().tenantId(), organizationId, agencyId, tuple.getT2().userId(),
                                tuple.getT1().reason())
                        .then(listAgenciesUseCase.listAgencies(organizationId)
                                .filter(agency -> agency.id().equals(agencyId))
                                .singleOrEmpty()))
                .map(AgencyResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency suspended.")));
    }

    @PostMapping("/{agencyId}/close")
    @PreAuthorize("@businessAccessPolicy.canGovernAgencies(authentication)")
    public Mono<ResponseEntity<ApiResponse<AgencyResponse>>> closeAgency(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("agencyId") UUID agencyId,
            @RequestBody Mono<GovernanceActionRequest> requestMono) {
        return requestMono.defaultIfEmpty(new GovernanceActionRequest(null))
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> closeAgencyUseCase
                        .closeAgency(tuple.getT2().tenantId(), organizationId, agencyId, tuple.getT2().userId(),
                                tuple.getT1().reason())
                        .then(listAgenciesUseCase.listAgencies(organizationId)
                                .filter(agency -> agency.id().equals(agencyId))
                                .singleOrEmpty()))
                .map(AgencyResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency closed.")));
    }
}
