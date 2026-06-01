package yowyob.comops.api.product.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface DeleteProductUseCase {

    Mono<Void> deleteProduct(UUID productId);
}
