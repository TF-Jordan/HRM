package yowyob.comops.api.product.application.port.in;

import yowyob.comops.api.product.domain.model.Product;
import reactor.core.publisher.Mono;

public interface CreateProductUseCase {

    Mono<Product> createProduct(CreateProductCommand command);
}
