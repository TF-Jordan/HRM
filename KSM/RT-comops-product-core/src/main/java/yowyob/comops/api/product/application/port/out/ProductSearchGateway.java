package yowyob.comops.api.product.application.port.out;

import yowyob.comops.api.product.domain.model.ProductSearchResult;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ProductSearchGateway {

    Flux<ProductSearchResult> search(UUID tenantId, UUID organizationId, String query, String familyCode, String status);
}
