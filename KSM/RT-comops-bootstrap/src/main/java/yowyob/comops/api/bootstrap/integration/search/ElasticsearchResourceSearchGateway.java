package yowyob.comops.api.bootstrap.integration.search;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import yowyob.comops.api.kernel.adapter.out.search.ResourceSearchDocument;
import yowyob.comops.api.resource.application.port.out.ResourceSearchGateway;
import yowyob.comops.api.resource.domain.model.MaterialResourceSearchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@ConditionalOnProperty(prefix = "iwm.search.elasticsearch", name = "enabled", havingValue = "true")
public class ElasticsearchResourceSearchGateway implements ResourceSearchGateway {

    private final ReactiveElasticsearchOperations operations;

    public ElasticsearchResourceSearchGateway(ReactiveElasticsearchOperations operations) {
        this.operations = operations;
    }

    @Override
    public Flux<MaterialResourceSearchResult> search(UUID tenantId, UUID organizationId, UUID agencyId, String query,
            String category, String status) {
        List<Query> filters = new ArrayList<>();
        List<Query> mustClauses = new ArrayList<>();
        filters.add(ElasticsearchQuerySupport.term("tenantId", tenantId.toString()));
        filters.add(ElasticsearchQuerySupport.term("organizationId", organizationId.toString()));
        if (agencyId != null) {
            filters.add(ElasticsearchQuerySupport.term("agencyId", agencyId.toString()));
        }
        if (ElasticsearchQuerySupport.hasText(category)) {
            filters.add(ElasticsearchQuerySupport.term("category", category.trim().toUpperCase()));
        }
        if (ElasticsearchQuerySupport.hasText(status)) {
            filters.add(ElasticsearchQuerySupport.term("status", status.trim().toUpperCase()));
        }
        if (ElasticsearchQuerySupport.hasText(query)) {
            String normalized = query.trim();
            mustClauses.add(ElasticsearchQuerySupport.should(
                    ElasticsearchQuerySupport.match("name", normalized),
                    ElasticsearchQuerySupport.term("resourceCode", normalized.toUpperCase()),
                    ElasticsearchQuerySupport.term("serialNumber", normalized.toUpperCase()),
                    ElasticsearchQuerySupport.term("ipAddress", normalized),
                    ElasticsearchQuerySupport.term("macAddress", normalized.toUpperCase())));
        }
        return operations.search(
                        ElasticsearchQuerySupport.nativeQuery(filters, mustClauses),
                        ResourceSearchDocument.class,
                        IndexCoordinates.of("iwm-resource-search-v1"))
                .map(SearchHit::getContent)
                .map(this::toResult);
    }

    private MaterialResourceSearchResult toResult(ResourceSearchDocument source) {
        return new MaterialResourceSearchResult(
                source.id(),
                source.tenantId(),
                source.organizationId(),
                source.agencyId(),
                source.resourceCode(),
                source.name(),
                source.category(),
                source.serialNumber(),
                source.status(),
                source.latitude(),
                source.longitude(),
                source.ipAddress(),
                source.macAddress());
    }
}
