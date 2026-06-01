package yowyob.comops.api.bootstrap.integration.inventory;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import yowyob.comops.api.inventory.application.port.out.AssetPortfolioInsightGateway;
import yowyob.comops.api.resource.application.service.AssetPortfolioApplicationService;

@Component
public class ResourceCoreInventoryAssetPortfolioGateway implements AssetPortfolioInsightGateway {

    private final AssetPortfolioApplicationService assetPortfolioApplicationService;

    public ResourceCoreInventoryAssetPortfolioGateway(
            AssetPortfolioApplicationService assetPortfolioApplicationService) {
        this.assetPortfolioApplicationService = assetPortfolioApplicationService;
    }

    @Override
    public Mono<AssetPortfolioSnapshot> organizationPortfolio(UUID tenantId, UUID organizationId) {
        return assetPortfolioApplicationService.organizationPortfolio(tenantId, organizationId)
                .map(this::toSnapshot);
    }

    @Override
    public Mono<AssetPortfolioSnapshot> agencyPortfolio(UUID tenantId, UUID organizationId, UUID agencyId) {
        return assetPortfolioApplicationService.agencyPortfolio(tenantId, organizationId, agencyId)
                .map(this::toSnapshot);
    }

    private AssetPortfolioSnapshot toSnapshot(AssetPortfolioApplicationService.AssetPortfolioView view) {
        return new AssetPortfolioSnapshot(
                view.totalResources(),
                view.assignedResources(),
                view.reservedResources(),
                view.openMaintenanceCount());
    }
}
