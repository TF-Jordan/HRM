package yowyob.comops.api.product.application.port.out;

import yowyob.comops.api.product.domain.model.ProductPrice;
import java.time.Instant;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductPriceRepository {

    Mono<ProductPrice> save(ProductPrice price);

    Flux<ProductPrice> findByProductId(UUID tenantId, UUID productId);

    Mono<ProductPrice> findEffectivePrice(UUID tenantId, UUID productId, String priceType, Instant at);
}
