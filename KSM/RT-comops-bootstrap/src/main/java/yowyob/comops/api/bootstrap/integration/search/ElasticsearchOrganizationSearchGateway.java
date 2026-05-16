package yowyob.comops.api.bootstrap.integration.search;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import yowyob.comops.api.kernel.adapter.out.search.OrganizationSearchDocument;
import yowyob.comops.api.organization.application.port.out.OrganizationSearchGateway;
import yowyob.comops.api.organization.domain.model.OrganizationSearchResult;
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
public class ElasticsearchOrganizationSearchGateway implements OrganizationSearchGateway {

    private final ReactiveElasticsearchOperations operations;

    public ElasticsearchOrganizationSearchGateway(ReactiveElasticsearchOperations operations) {
        this.operations = operations;
    }

    @Override
    public Flux<OrganizationSearchResult> search(UUID tenantId, String query, String organizationType) {
        List<Query> filters = new ArrayList<>();
        List<Query> mustClauses = new ArrayList<>();
        filters.add(ElasticsearchQuerySupport.term("tenantId", tenantId.toString()));
        if (ElasticsearchQuerySupport.hasText(organizationType)) {
            filters.add(ElasticsearchQuerySupport.term("service", organizationType.trim().toUpperCase()));
        }
        if (ElasticsearchQuerySupport.hasText(query)) {
            String normalized = query.trim();
            mustClauses.add(ElasticsearchQuerySupport.should(
                    ElasticsearchQuerySupport.match("shortName", normalized),
                    ElasticsearchQuerySupport.match("longName", normalized),
                    ElasticsearchQuerySupport.term("code", normalized.toUpperCase())));
        }
        return operations.search(
                        ElasticsearchQuerySupport.nativeQuery(filters, mustClauses),
                        OrganizationSearchDocument.class,
                        IndexCoordinates.of("iwm-organization-search-v1"))
                .map(SearchHit::getContent)
                .map(this::toResult);
    }

    private OrganizationSearchResult toResult(OrganizationSearchDocument source) {
        return new OrganizationSearchResult(
                source.id(),
                source.tenantId(),
                source.businessActorId(),
                source.code(),
                source.service(),
                source.shortName(),
                source.longName(),
                source.legalForm(),
                source.isActive(),
                source.status());
    }
}
