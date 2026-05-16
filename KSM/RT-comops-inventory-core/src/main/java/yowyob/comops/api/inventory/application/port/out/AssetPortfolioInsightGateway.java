package yowyob.comops.api.inventory.application.port.out;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface AssetPortfolioInsightGateway {

    Mono<AssetPortfolioSnapshot> organizationPortfolio(UUID tenantId, UUID organizationId);

    Mono<AssetPortfolioSnapshot> agencyPortfolio(UUID tenantId, UUID organizationId, UUID agencyId);

    record AssetPortfolioSnapshot(
            int totalResources,
            long assignedResources,
            long reservedResources,
            long openMaintenanceCount) {
    }
}
