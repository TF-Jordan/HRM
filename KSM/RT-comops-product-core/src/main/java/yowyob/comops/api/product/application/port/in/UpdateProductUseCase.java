package yowyob.comops.api.product.application.port.in;

import yowyob.comops.api.product.domain.model.Product;
import reactor.core.publisher.Mono;

public interface UpdateProductUseCase {

    Mono<Product> updateProduct(UpdateProductCommand command);
}
