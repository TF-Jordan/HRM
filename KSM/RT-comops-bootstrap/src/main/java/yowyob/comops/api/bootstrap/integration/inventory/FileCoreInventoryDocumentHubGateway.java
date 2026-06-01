package yowyob.comops.api.bootstrap.integration.inventory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import yowyob.comops.api.file.application.service.DocumentHubApplicationService;
import yowyob.comops.api.inventory.application.port.out.DocumentHubInsightGateway;

@Component
public class FileCoreInventoryDocumentHubGateway implements DocumentHubInsightGateway {

    private final DocumentHubApplicationService documentHubApplicationService;

    public FileCoreInventoryDocumentHubGateway(DocumentHubApplicationService documentHubApplicationService) {
        this.documentHubApplicationService = documentHubApplicationService;
    }

    @Override
    public Mono<DocumentHubOverviewSnapshot> organizationOverview(UUID tenantId, UUID organizationId) {
        return documentHubApplicationService.overview(tenantId, organizationId)
                .map(overview -> new DocumentHubOverviewSnapshot(
                        overview.organizationId(),
                        overview.totalDocuments(),
                        overview.countsByTargetType(),
                        overview.countsByCategory()));
    }

    @Override
    public Mono<TargetDocumentSummary> summarizeTarget(UUID tenantId, String targetType, UUID targetId) {
        return documentHubApplicationService.listByTarget(tenantId, targetType, targetId)
                .collectList()
                .map(links -> {
                    Map<String, Long> countsByCategory = new LinkedHashMap<>();
                    links.forEach(link -> countsByCategory.merge(link.documentCategory(), 1L, Long::sum));
                    return new TargetDocumentSummary(targetType, targetId, links.size(), countsByCategory);
                });
    }
}
