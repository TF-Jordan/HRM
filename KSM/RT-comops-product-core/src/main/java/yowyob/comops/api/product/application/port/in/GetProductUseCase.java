package yowyob.comops.api.product.application.port.in;

import yowyob.comops.api.product.domain.model.Product;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface GetProductUseCase {

    Mono<Product> getProduct(UUID productId);
}
