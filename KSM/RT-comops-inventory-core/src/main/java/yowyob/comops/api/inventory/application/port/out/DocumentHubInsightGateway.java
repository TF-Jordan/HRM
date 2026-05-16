package yowyob.comops.api.inventory.application.port.out;

import java.util.Map;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface DocumentHubInsightGateway {

    Mono<DocumentHubOverviewSnapshot> organizationOverview(UUID tenantId, UUID organizationId);

    Mono<TargetDocumentSummary> summarizeTarget(UUID tenantId, String targetType, UUID targetId);

    record DocumentHubOverviewSnapshot(
            UUID organizationId,
            int totalDocuments,
            Map<String, Long> countsByTargetType,
            Map<String, Long> countsByCategory) {
    }

    record TargetDocumentSummary(
            String targetType,
            UUID targetId,
            int totalDocuments,
            Map<String, Long> countsByCategory) {
    }
}
