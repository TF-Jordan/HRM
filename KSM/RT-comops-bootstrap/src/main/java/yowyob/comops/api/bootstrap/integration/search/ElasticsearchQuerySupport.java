package yowyob.comops.api.bootstrap.integration.search;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.query.Criteria;

final class ElasticsearchQuerySupport {

    private ElasticsearchQuerySupport() {
    }

    static Criteria tenantAndOrganization(UUID tenantId, UUID organizationId) {
        return new Criteria("tenantId").is(tenantId.toString())
                .and("organizationId").is(organizationId.toString());
    }

    static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    static Query term(String field, String value) {
        return Query.of(query -> query.term(term -> term.field(field).value(value)));
    }

    static Query match(String field, String value) {
        return Query.of(query -> query.match(match -> match.field(field).query(value)));
    }

    static Query rangeGte(String field, Double value) {
        return Query.of(query -> query.range(range -> range.number(number -> number.field(field).gte(value))));
    }

    static Query should(Query... clauses) {
        return Query.of(query -> query.bool(bool -> {
            for (Query clause : clauses) {
                bool.should(clause);
            }
            return bool.minimumShouldMatch("1");
        }));
    }

    static NativeQuery nativeQuery(List<Query> filters, List<Query> mustClauses) {
        return nativeQuery(filters, mustClauses, PageRequest.of(0, 20));
    }

    static NativeQuery nativeQuery(List<Query> filters, List<Query> mustClauses,
            org.springframework.data.domain.Pageable pageable) {
        BoolQuery.Builder bool = new BoolQuery.Builder();
        filters.forEach(bool::filter);
        mustClauses.forEach(bool::must);
        return NativeQuery.builder()
                .withQuery(bool.build()._toQuery())
                .withPageable(pageable)
                .build();
    }
}
