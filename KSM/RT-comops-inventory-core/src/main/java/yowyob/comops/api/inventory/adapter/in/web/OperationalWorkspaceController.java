package yowyob.comops.api.inventory.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.inventory.application.service.OperationalWorkspaceApplicationService;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'inventory:write')")
public class OperationalWorkspaceController {

    private final OperationalWorkspaceApplicationService operationalWorkspaceApplicationService;

    public OperationalWorkspaceController(
            OperationalWorkspaceApplicationService operationalWorkspaceApplicationService) {
        this.operationalWorkspaceApplicationService = operationalWorkspaceApplicationService;
    }

    @GetMapping("/organizations/{organizationId}/agencies/{agencyId}/operational-site")
    public Mono<ResponseEntity<ApiResponse<OperationalWorkspaceApplicationService.OperationalSiteView>>> agencyOperationalSite(
            @PathVariable UUID organizationId, @PathVariable UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> operationalWorkspaceApplicationService.agencyOperationalSite(context.tenantId(),
                        organizationId, agencyId))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Operational site fetched.")));
    }

    @GetMapping("/warehouses/{warehouseId}/operational-site")
    public Mono<ResponseEntity<ApiResponse<OperationalWorkspaceApplicationService.OperationalSiteView>>> warehouseOperationalSite(
            @PathVariable UUID warehouseId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> operationalWorkspaceApplicationService.warehouseOperationalSite(context.tenantId(),
                        warehouseId))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Warehouse operational site fetched.")));
    }

    @GetMapping("/organizations/{organizationId}/generalized-inventory")
    public Mono<ResponseEntity<ApiResponse<OperationalWorkspaceApplicationService.GeneralizedInventoryView>>> organizationInventory(
            @PathVariable UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> operationalWorkspaceApplicationService.organizationInventory(context.tenantId(),
                        organizationId))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Generalized inventory fetched.")));
    }

    @GetMapping("/organizations/{organizationId}/agencies/{agencyId}/generalized-inventory")
    public Mono<ResponseEntity<ApiResponse<OperationalWorkspaceApplicationService.GeneralizedInventoryView>>> agencyInventory(
            @PathVariable UUID organizationId, @PathVariable UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> operationalWorkspaceApplicationService.agencyInventory(context.tenantId(),
                        organizationId, agencyId))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency generalized inventory fetched.")));
    }

    @GetMapping("/warehouses/{warehouseId}/generalized-inventory")
    public Mono<ResponseEntity<ApiResponse<OperationalWorkspaceApplicationService.GeneralizedInventoryView>>> warehouseInventory(
            @PathVariable UUID warehouseId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> operationalWorkspaceApplicationService.warehouseInventory(context.tenantId(),
                        warehouseId))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Warehouse generalized inventory fetched.")));
    }

    @GetMapping("/organizations/{organizationId}/service-workspaces/{workspaceCode}")
    public Mono<ResponseEntity<ApiResponse<OperationalWorkspaceApplicationService.ServiceWorkspaceView>>> serviceWorkspace(
            @PathVariable UUID organizationId, @PathVariable String workspaceCode) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> operationalWorkspaceApplicationService.serviceWorkspace(context.tenantId(),
                        organizationId, workspaceCode))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Service workspace fetched.")));
    }
}
