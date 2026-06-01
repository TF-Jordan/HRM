package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.organization.application.port.in.CreateAgencyCommand;
import yowyob.comops.api.organization.application.port.in.CreateAgencyUseCase;
import yowyob.comops.api.organization.application.port.in.DeactivateAgencyUseCase;
import yowyob.comops.api.organization.application.port.in.ListAgenciesUseCase;
import yowyob.comops.api.organization.application.port.in.UpdateAgencyCommand;
import yowyob.comops.api.organization.application.port.in.UpdateAgencyUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/warehouses")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
public class WarehouseController {

    private static final String WAREHOUSE_TYPE = "WAREHOUSE";

    private final CreateAgencyUseCase createAgencyUseCase;
    private final ListAgenciesUseCase listAgenciesUseCase;
    private final UpdateAgencyUseCase updateAgencyUseCase;
    private final DeactivateAgencyUseCase deactivateAgencyUseCase;

    public WarehouseController(CreateAgencyUseCase createAgencyUseCase, ListAgenciesUseCase listAgenciesUseCase,
            UpdateAgencyUseCase updateAgencyUseCase, DeactivateAgencyUseCase deactivateAgencyUseCase) {
        this.createAgencyUseCase = createAgencyUseCase;
        this.listAgenciesUseCase = listAgenciesUseCase;
        this.updateAgencyUseCase = updateAgencyUseCase;
        this.deactivateAgencyUseCase = deactivateAgencyUseCase;
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<AgencyResponse>>>> listWarehouses() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listAgenciesUseCase.listAgencies(context.organizationId())
                        .filter(agency -> WAREHOUSE_TYPE.equals(agency.agencyType()))
                        .map(AgencyResponse::from)
                        .collectList()
                        .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Warehouses retrieved."))));
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<AgencyResponse>>> createWarehouse(
            @Valid @RequestBody Mono<CreateAgencyRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> createAgencyUseCase.createAgency(new CreateAgencyCommand(
                        tuple.getT2().tenantId(),
                        requireOrganization(tuple.getT2().organizationId()),
                        tuple.getT1().code(),
                        null, null,
                        tuple.getT1().name(),
                        null, null, false, true,
                        null, null, null, null, false, false,
                        null, null, null, null,
                        null, null, null, null, null, null,
                        null, null, null, null, null, null,
                        false, true, null,
                        WAREHOUSE_TYPE)))
                .map(AgencyResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Warehouse created.")));
    }

    @PatchMapping("/{warehouseId}")
    public Mono<ResponseEntity<ApiResponse<AgencyResponse>>> updateWarehouse(@PathVariable UUID warehouseId,
            @Valid @RequestBody Mono<UpdateWarehouseRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> updateAgencyUseCase.updateAgency(new UpdateAgencyCommand(
                        tuple.getT2().tenantId(),
                        warehouseId,
                        tuple.getT1().code(),
                        null, null,
                        tuple.getT1().name(),
                        null, null, false, true,
                        null, null, null, null, false, false,
                        null, null, null, null,
                        null, null, null, null, null, null,
                        null, null, null, null, null, null,
                        false, true, null,
                        WAREHOUSE_TYPE)))
                .filter(agency -> WAREHOUSE_TYPE.equals(agency.agencyType()))
                .map(AgencyResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Warehouse updated.")));
    }

    @DeleteMapping("/{warehouseId}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteWarehouse(@PathVariable UUID warehouseId) {
        return deactivateAgencyUseCase.deactivateAgency(warehouseId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Warehouse deactivated.")));
    }

    private UUID requireOrganization(UUID organizationId) {
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId is required in X-Organization-Id header");
        }
        return organizationId;
    }
}
