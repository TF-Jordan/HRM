package yowyob.comops.api.product.application.port.in;

import yowyob.comops.api.product.domain.model.Product;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListProductsUseCase {

    Flux<Product> listProducts(UUID organizationId, String familyCode, String status);
}
