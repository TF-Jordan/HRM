package yowyob.comops.api.bootstrap.integration.search;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import yowyob.comops.api.kernel.adapter.out.search.ProductSearchDocument;
import yowyob.comops.api.product.application.port.out.ProductSearchGateway;
import yowyob.comops.api.product.domain.model.ProductSearchResult;
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
public class ElasticsearchProductSearchGateway implements ProductSearchGateway {

    private final ReactiveElasticsearchOperations operations;

    public ElasticsearchProductSearchGateway(ReactiveElasticsearchOperations operations) {
        this.operations = operations;
    }

    @Override
    public Flux<ProductSearchResult> search(UUID tenantId, UUID organizationId, String query, String familyCode,
            String status) {
        List<Query> filters = new ArrayList<>();
        List<Query> mustClauses = new ArrayList<>();
        filters.add(ElasticsearchQuerySupport.term("tenantId", tenantId.toString()));
        filters.add(ElasticsearchQuerySupport.term("organizationId", organizationId.toString()));
        if (ElasticsearchQuerySupport.hasText(familyCode)) {
            filters.add(ElasticsearchQuerySupport.term("familyCode", familyCode.trim().toUpperCase()));
        }
        if (ElasticsearchQuerySupport.hasText(status)) {
            filters.add(ElasticsearchQuerySupport.term("status", status.trim().toUpperCase()));
        }
        if (ElasticsearchQuerySupport.hasText(query)) {
            String normalized = query.trim();
            mustClauses.add(ElasticsearchQuerySupport.should(
                    ElasticsearchQuerySupport.match("name", normalized),
                    ElasticsearchQuerySupport.term("sku", normalized.toUpperCase()),
                    ElasticsearchQuerySupport.term("barcode", normalized),
                    ElasticsearchQuerySupport.match("description", normalized),
                    ElasticsearchQuerySupport.match("variantLabel", normalized)));
        }
        return operations.search(
                        ElasticsearchQuerySupport.nativeQuery(filters, mustClauses),
                        ProductSearchDocument.class,
                        IndexCoordinates.of("iwm-product-search-v1"))
                .map(SearchHit::getContent)
                .map(this::toResult);
    }

    private ProductSearchResult toResult(ProductSearchDocument source) {
        return new ProductSearchResult(
                source.id(),
                source.tenantId(),
                source.organizationId(),
                source.sku(),
                source.name(),
                source.familyCode(),
                source.variantLabel(),
                source.barcode(),
                source.description(),
                source.unitPrice(),
                source.currency(),
                source.status());
    }
}
