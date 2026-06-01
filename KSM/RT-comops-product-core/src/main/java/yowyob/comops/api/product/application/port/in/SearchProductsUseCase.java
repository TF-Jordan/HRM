package yowyob.comops.api.product.application.port.in;

import yowyob.comops.api.product.domain.model.ProductSearchResult;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface SearchProductsUseCase {

    Flux<ProductSearchResult> searchProducts(UUID organizationId, String query, String familyCode, String status);
}
