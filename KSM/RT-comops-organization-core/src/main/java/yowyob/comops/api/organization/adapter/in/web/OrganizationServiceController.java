package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.organization.application.port.in.GetOrganizationServiceEntitlementsUseCase;
import yowyob.comops.api.organization.application.port.in.ListPlatformServicesUseCase;
import yowyob.comops.api.organization.application.port.in.SubscribeOrganizationServiceUseCase;
import yowyob.comops.api.organization.application.port.in.UnsubscribeOrganizationServiceUseCase;
import yowyob.comops.api.organization.application.port.in.UpdateOrganizationServiceQuotaUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/organizations")
public class OrganizationServiceController {

    private final ListPlatformServicesUseCase listPlatformServicesUseCase;
    private final GetOrganizationServiceEntitlementsUseCase getOrganizationServiceEntitlementsUseCase;
    private final SubscribeOrganizationServiceUseCase subscribeOrganizationServiceUseCase;
    private final UnsubscribeOrganizationServiceUseCase unsubscribeOrganizationServiceUseCase;
    private final UpdateOrganizationServiceQuotaUseCase updateOrganizationServiceQuotaUseCase;

    public OrganizationServiceController(ListPlatformServicesUseCase listPlatformServicesUseCase,
            GetOrganizationServiceEntitlementsUseCase getOrganizationServiceEntitlementsUseCase,
            SubscribeOrganizationServiceUseCase subscribeOrganizationServiceUseCase,
            UnsubscribeOrganizationServiceUseCase unsubscribeOrganizationServiceUseCase,
            UpdateOrganizationServiceQuotaUseCase updateOrganizationServiceQuotaUseCase) {
        this.listPlatformServicesUseCase = listPlatformServicesUseCase;
        this.getOrganizationServiceEntitlementsUseCase = getOrganizationServiceEntitlementsUseCase;
        this.subscribeOrganizationServiceUseCase = subscribeOrganizationServiceUseCase;
        this.unsubscribeOrganizationServiceUseCase = unsubscribeOrganizationServiceUseCase;
        this.updateOrganizationServiceQuotaUseCase = updateOrganizationServiceQuotaUseCase;
    }

    @GetMapping("/services/catalog")
    @PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
    public Mono<ResponseEntity<ApiResponse<List<OrganizationServiceCatalogResponse>>>> listCatalog() {
        return listPlatformServicesUseCase.listPlatformServices()
                .map(OrganizationServiceCatalogResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Platform services catalog retrieved.")));
    }

    @GetMapping("/{organizationId}/services")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
    public Mono<ResponseEntity<ApiResponse<OrganizationServicesResponse>>> getOrganizationServices(
            @PathVariable("organizationId") UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> getOrganizationServiceEntitlementsUseCase.getOrganizationServiceEntitlements(
                        context.tenantId(), organizationId))
                .map(OrganizationServicesResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization services retrieved.")));
    }

    @PostMapping("/{organizationId}/services")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
    public Mono<ResponseEntity<ApiResponse<OrganizationServicesResponse>>> subscribe(
            @PathVariable("organizationId") UUID organizationId,
            @Valid @RequestBody Mono<SubscribeOrganizationServiceRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> subscribeOrganizationServiceUseCase.subscribeOrganizationService(
                        tuple.getT2().tenantId(),
                        organizationId,
                        tuple.getT1().serviceCode(),
                        tuple.getT1().requestQuotaLimit(),
                        tuple.getT1().requestQuotaWindowSeconds()))
                .map(OrganizationServicesResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization service subscription updated.")));
    }

    @PatchMapping("/{organizationId}/services/{serviceCode}/quota")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
    public Mono<ResponseEntity<ApiResponse<OrganizationServicesResponse>>> updateQuota(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("serviceCode") String serviceCode,
            @Valid @RequestBody Mono<UpdateOrganizationServiceQuotaRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> updateOrganizationServiceQuotaUseCase.updateOrganizationServiceQuota(
                        tuple.getT2().tenantId(),
                        organizationId,
                        serviceCode,
                        tuple.getT1().requestQuotaLimit(),
                        tuple.getT1().requestQuotaWindowSeconds()))
                .map(OrganizationServicesResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization service quota updated.")));
    }

    @DeleteMapping("/{organizationId}/services/{serviceCode}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
    public Mono<ResponseEntity<ApiResponse<OrganizationServicesResponse>>> unsubscribe(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("serviceCode") String serviceCode) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> unsubscribeOrganizationServiceUseCase.unsubscribeOrganizationService(
                        context.tenantId(), organizationId, serviceCode))
                .map(OrganizationServicesResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization service subscription updated.")));
    }
}
