package yowyob.comops.api.resource.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.resource.application.service.AssetPortfolioApplicationService;
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
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'resources:write')")
public class AssetPortfolioController {

    private final AssetPortfolioApplicationService assetPortfolioApplicationService;

    public AssetPortfolioController(AssetPortfolioApplicationService assetPortfolioApplicationService) {
        this.assetPortfolioApplicationService = assetPortfolioApplicationService;
    }

    @GetMapping("/organizations/{organizationId}/asset-portfolio")
    public Mono<ResponseEntity<ApiResponse<AssetPortfolioApplicationService.AssetPortfolioView>>> organizationPortfolio(
            @PathVariable UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> assetPortfolioApplicationService.organizationPortfolio(context.tenantId(),
                        organizationId))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization asset portfolio fetched.")));
    }

    @GetMapping("/organizations/{organizationId}/agencies/{agencyId}/asset-portfolio")
    public Mono<ResponseEntity<ApiResponse<AssetPortfolioApplicationService.AssetPortfolioView>>> agencyPortfolio(
            @PathVariable UUID organizationId, @PathVariable UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> assetPortfolioApplicationService.agencyPortfolio(context.tenantId(), organizationId,
                        agencyId))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency asset portfolio fetched.")));
    }

    @GetMapping("/warehouses/{warehouseId}/asset-portfolio")
    public Mono<ResponseEntity<ApiResponse<AssetPortfolioApplicationService.AssetPortfolioView>>> warehousePortfolio(
            @PathVariable UUID warehouseId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> assetPortfolioApplicationService.agencyPortfolio(context.tenantId(),
                        context.organizationId(), warehouseId))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Warehouse asset portfolio fetched.")));
    }
}
