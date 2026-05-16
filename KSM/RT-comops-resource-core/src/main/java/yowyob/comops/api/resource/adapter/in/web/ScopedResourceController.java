package yowyob.comops.api.resource.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.organization.domain.model.Agency;
import yowyob.comops.api.resource.application.port.in.ListMaterialResourcesUseCase;
import yowyob.comops.api.resource.application.port.in.RegisterMaterialResourceCommand;
import yowyob.comops.api.resource.application.port.in.RegisterMaterialResourceUseCase;
import yowyob.comops.api.resource.application.port.in.SearchMaterialResourcesUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'resources:write')")
public class ScopedResourceController {

    private static final String WAREHOUSE_TYPE = "WAREHOUSE";

    private final RegisterMaterialResourceUseCase registerMaterialResourceUseCase;
    private final ListMaterialResourcesUseCase listMaterialResourcesUseCase;
    private final SearchMaterialResourcesUseCase searchMaterialResourcesUseCase;
    private final OrganizationRepository organizationRepository;
    private final AgencyRepository agencyRepository;

    public ScopedResourceController(RegisterMaterialResourceUseCase registerMaterialResourceUseCase,
            ListMaterialResourcesUseCase listMaterialResourcesUseCase,
            SearchMaterialResourcesUseCase searchMaterialResourcesUseCase,
            OrganizationRepository organizationRepository,
            AgencyRepository agencyRepository) {
        this.registerMaterialResourceUseCase = registerMaterialResourceUseCase;
        this.listMaterialResourcesUseCase = listMaterialResourcesUseCase;
        this.searchMaterialResourcesUseCase = searchMaterialResourcesUseCase;
        this.organizationRepository = organizationRepository;
        this.agencyRepository = agencyRepository;
    }

    @GetMapping("/organizations/{organizationId}/resources")
    public Mono<ResponseEntity<ApiResponse<List<MaterialResourceResponse>>>> listOrganizationResources(
            @PathVariable UUID organizationId,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "status", required = false) String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> ensureOrganizationExists(context.tenantId(), organizationId)
                        .then(listMaterialResourcesUseCase.listResources(context.tenantId(), organizationId, null,
                                category, status)
                                .map(MaterialResourceResponse::from)
                                .collectList()))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization resources retrieved.")));
    }

    @PostMapping("/organizations/{organizationId}/resources")
    public Mono<ResponseEntity<ApiResponse<MaterialResourceResponse>>> registerOrganizationResource(
            @PathVariable UUID organizationId,
            @Valid @RequestBody Mono<OrganizationScopedResourceRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> ensureAgencyBelongsToOrganization(tuple.getT2().tenantId(), organizationId,
                        tuple.getT1().agencyId())
                        .then(registerMaterialResourceUseCase.register(new RegisterMaterialResourceCommand(
                                tuple.getT2().tenantId(), organizationId, tuple.getT1().agencyId(),
                                tuple.getT1().resourceCode(), tuple.getT1().name(), tuple.getT1().category(),
                                tuple.getT1().serialNumber(), tuple.getT1().latitude(), tuple.getT1().longitude(),
                                tuple.getT1().ipAddress(), tuple.getT1().macAddress()))))
                .map(MaterialResourceResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Organization resource registered.")));
    }

    @GetMapping("/organizations/{organizationId}/resources/search")
    public Mono<ResponseEntity<ApiResponse<List<MaterialResourceSearchResponse>>>> searchOrganizationResources(
            @PathVariable UUID organizationId,
            @RequestParam("q") String query,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "status", required = false) String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> ensureOrganizationExists(context.tenantId(), organizationId)
                        .then(searchMaterialResourcesUseCase.searchResources(context.tenantId(), organizationId, null,
                                query, category, status)
                                .map(MaterialResourceSearchResponse::from)
                                .collectList()))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                        "Organization resource search results retrieved.")));
    }

    @GetMapping("/organizations/{organizationId}/agencies/{agencyId}/resources")
    public Mono<ResponseEntity<ApiResponse<List<MaterialResourceResponse>>>> listAgencyResources(
            @PathVariable UUID organizationId,
            @PathVariable UUID agencyId,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "status", required = false) String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> ensureAgencyBelongsToOrganization(context.tenantId(), organizationId, agencyId)
                        .then(listMaterialResourcesUseCase.listResources(context.tenantId(), organizationId, agencyId,
                                category, status)
                                .map(MaterialResourceResponse::from)
                                .collectList()))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency resources retrieved.")));
    }

    @PostMapping("/organizations/{organizationId}/agencies/{agencyId}/resources")
    public Mono<ResponseEntity<ApiResponse<MaterialResourceResponse>>> registerAgencyResource(
            @PathVariable UUID organizationId,
            @PathVariable UUID agencyId,
            @Valid @RequestBody Mono<AgencyScopedResourceRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> ensureAgencyBelongsToOrganization(tuple.getT2().tenantId(), organizationId, agencyId)
                        .then(registerMaterialResourceUseCase.register(new RegisterMaterialResourceCommand(
                                tuple.getT2().tenantId(), organizationId, agencyId,
                                tuple.getT1().resourceCode(), tuple.getT1().name(), tuple.getT1().category(),
                                tuple.getT1().serialNumber(), tuple.getT1().latitude(), tuple.getT1().longitude(),
                                tuple.getT1().ipAddress(), tuple.getT1().macAddress()))))
                .map(MaterialResourceResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Agency resource registered.")));
    }

    @GetMapping("/organizations/{organizationId}/agencies/{agencyId}/resources/search")
    public Mono<ResponseEntity<ApiResponse<List<MaterialResourceSearchResponse>>>> searchAgencyResources(
            @PathVariable UUID organizationId,
            @PathVariable UUID agencyId,
            @RequestParam("q") String query,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "status", required = false) String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> ensureAgencyBelongsToOrganization(context.tenantId(), organizationId, agencyId)
                        .then(searchMaterialResourcesUseCase.searchResources(context.tenantId(), organizationId,
                                agencyId, query, category, status)
                                .map(MaterialResourceSearchResponse::from)
                                .collectList()))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                        "Agency resource search results retrieved.")));
    }

    @GetMapping("/warehouses/{warehouseId}/resources")
    public Mono<ResponseEntity<ApiResponse<List<MaterialResourceResponse>>>> listWarehouseResources(
            @PathVariable UUID warehouseId,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "status", required = false) String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> ensureWarehouse(context.tenantId(), warehouseId)
                        .flatMap(warehouse -> listMaterialResourcesUseCase.listResources(context.tenantId(),
                                        warehouse.organizationId(), warehouseId, category, status)
                                .map(MaterialResourceResponse::from)
                                .collectList()))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Warehouse resources retrieved.")));
    }

    @PostMapping("/warehouses/{warehouseId}/resources")
    public Mono<ResponseEntity<ApiResponse<MaterialResourceResponse>>> registerWarehouseResource(
            @PathVariable UUID warehouseId,
            @Valid @RequestBody Mono<WarehouseScopedResourceRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> ensureWarehouse(tuple.getT2().tenantId(), warehouseId)
                        .flatMap(warehouse -> registerMaterialResourceUseCase.register(new RegisterMaterialResourceCommand(
                                tuple.getT2().tenantId(), warehouse.organizationId(), warehouseId,
                                tuple.getT1().resourceCode(), tuple.getT1().name(), tuple.getT1().category(),
                                tuple.getT1().serialNumber(), tuple.getT1().latitude(), tuple.getT1().longitude(),
                                tuple.getT1().ipAddress(), tuple.getT1().macAddress()))))
                .map(MaterialResourceResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Warehouse resource registered.")));
    }

    @GetMapping("/warehouses/{warehouseId}/resources/search")
    public Mono<ResponseEntity<ApiResponse<List<MaterialResourceSearchResponse>>>> searchWarehouseResources(
            @PathVariable UUID warehouseId,
            @RequestParam("q") String query,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "status", required = false) String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> ensureWarehouse(context.tenantId(), warehouseId)
                        .flatMap(warehouse -> searchMaterialResourcesUseCase.searchResources(context.tenantId(),
                                        warehouse.organizationId(), warehouseId, query, category, status)
                                .map(MaterialResourceSearchResponse::from)
                                .collectList()))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response,
                        "Warehouse resource search results retrieved.")));
    }

    private Mono<Void> ensureOrganizationExists(UUID tenantId, UUID organizationId) {
        return organizationRepository.findById(tenantId, organizationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("organization not found")))
                .then();
    }

    private Mono<Void> ensureAgencyBelongsToOrganization(UUID tenantId, UUID organizationId, UUID agencyId) {
        return ensureOrganizationExists(tenantId, organizationId)
                .then(agencyRepository.findById(tenantId, agencyId)
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

    private Mono<Agency> ensureWarehouse(UUID tenantId, UUID warehouseId) {
        return agencyRepository.findById(tenantId, warehouseId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("warehouse not found")))
                .flatMap(agency -> {
                    if (!WAREHOUSE_TYPE.equalsIgnoreCase(agency.agencyType())) {
                        return Mono.error(new IllegalArgumentException("agency is not a warehouse"));
                    }
                    if (!agency.active()) {
                        return Mono.error(new IllegalArgumentException("warehouse is not active"));
                    }
                    return Mono.just(agency);
                });
    }

    record OrganizationScopedResourceRequest(
            @NotNull UUID agencyId,
            @NotBlank String resourceCode,
            @NotBlank String name,
            @NotBlank String category,
            @NotBlank String serialNumber,
            Double latitude,
            Double longitude,
            String ipAddress,
            String macAddress) {
    }

    record AgencyScopedResourceRequest(
            @NotBlank String resourceCode,
            @NotBlank String name,
            @NotBlank String category,
            @NotBlank String serialNumber,
            Double latitude,
            Double longitude,
            String ipAddress,
            String macAddress) {
    }

    record WarehouseScopedResourceRequest(
            @NotBlank String resourceCode,
            @NotBlank String name,
            @NotBlank String category,
            @NotBlank String serialNumber,
            Double latitude,
            Double longitude,
            String ipAddress,
            String macAddress) {
    }
}
