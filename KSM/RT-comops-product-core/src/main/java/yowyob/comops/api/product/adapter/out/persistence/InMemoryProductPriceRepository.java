package yowyob.comops.api.product.adapter.out.persistence;

import yowyob.comops.api.product.application.port.out.ProductPriceRepository;
import yowyob.comops.api.product.domain.model.ProductPrice;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryProductPriceRepository implements ProductPriceRepository {

    private final Map<UUID, ProductPrice> prices = new ConcurrentHashMap<>();

    @Override
    public Mono<ProductPrice> save(ProductPrice price) {
        return Mono.fromSupplier(() -> {
            prices.put(price.id(), price);
            return price;
        });
    }

    @Override
    public Flux<ProductPrice> findByProductId(UUID tenantId, UUID productId) {
        return Flux.fromStream(prices.values().stream()
                .filter(price -> price.tenantId().equals(tenantId))
                .filter(price -> price.productId().equals(productId))
                .sorted(Comparator.comparing(ProductPrice::effectiveFrom).reversed()));
    }

    @Override
    public Mono<ProductPrice> findEffectivePrice(UUID tenantId, UUID productId, String priceType, Instant at) {
        return findByProductId(tenantId, productId)
                .filter(price -> price.priceType().equalsIgnoreCase(priceType))
                .filter(price -> !price.effectiveFrom().isAfter(at))
                .next();
    }
}
