package yowyob.comops.api.bootstrap.integration.search;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import yowyob.comops.api.common.domain.model.PartyType;
import yowyob.comops.api.kernel.adapter.out.search.ThirdPartySearchDocument;
import yowyob.comops.api.tp.application.port.out.ThirdPartySearchGateway;
import yowyob.comops.api.tp.domain.model.ThirdPartySearchResult;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
public class ElasticsearchThirdPartySearchGateway implements ThirdPartySearchGateway {

    private final ReactiveElasticsearchOperations operations;

    public ElasticsearchThirdPartySearchGateway(ReactiveElasticsearchOperations operations) {
        this.operations = operations;
    }

    @Override
    public Flux<ThirdPartySearchResult> search(UUID tenantId, UUID organizationId, String query, String role,
            Boolean prospect, String segment, Integer minimumQualificationScore, Boolean active, String followUpStatus,
            int page, int size) {
        List<Query> filters = new ArrayList<>();
        List<Query> mustClauses = new ArrayList<>();
        filters.add(ElasticsearchQuerySupport.term("tenantId", tenantId.toString()));
        filters.add(ElasticsearchQuerySupport.term("organizationId", organizationId.toString()));
        if (ElasticsearchQuerySupport.hasText(role)) {
            filters.add(ElasticsearchQuerySupport.term("roles", role.trim().toUpperCase()));
        }
        if (prospect != null) {
            filters.add(ElasticsearchQuerySupport.term("prospect", String.valueOf(prospect)));
        }
        if (ElasticsearchQuerySupport.hasText(segment)) {
            filters.add(ElasticsearchQuerySupport.term("segment", segment.trim().toUpperCase()));
        }
        if (minimumQualificationScore != null) {
            filters.add(
                    ElasticsearchQuerySupport.rangeGte("qualificationScore", minimumQualificationScore.doubleValue()));
        }
        if (ElasticsearchQuerySupport.hasText(query)) {
            String normalized = query.trim();
            mustClauses.add(ElasticsearchQuerySupport.should(
                    ElasticsearchQuerySupport.match("name", normalized),
                    ElasticsearchQuerySupport.match("displayName", normalized),
                    ElasticsearchQuerySupport.term("code", normalized.toUpperCase()),
                    ElasticsearchQuerySupport.term("referenceCode", normalized.toUpperCase())));
        }
        return operations.search(
                ElasticsearchQuerySupport.nativeQuery(filters, mustClauses),
                ThirdPartySearchDocument.class,
                IndexCoordinates.of("iwm-third-party-search-v1"))
                .map(SearchHit::getContent)
                .map(this::toResult);
    }

    private ThirdPartySearchResult toResult(ThirdPartySearchDocument source) {
        return new ThirdPartySearchResult(
                source.id(),
                source.tenantId(),
                source.organizationId(),
                PartyType.valueOf(source.partyType()),
                source.partyId(),
                source.code(),
                source.name(),
                source.type(),
                source.longName(),
                new LinkedHashSet<>(source.roles()),
                source.prospect(),
                source.accountingAccount(),
                source.segment(),
                source.qualificationScore(),
                source.enabled(),
                source.lastContactedAt(),
                source.nextFollowUpAt(),
                source.followUpStatus(),
                source.convertedAt());
    }
}
