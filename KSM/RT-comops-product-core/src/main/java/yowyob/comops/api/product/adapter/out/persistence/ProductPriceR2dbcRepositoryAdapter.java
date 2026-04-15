package yowyob.comops.api.product.adapter.out.persistence;

import yowyob.comops.api.product.application.port.out.ProductPriceRepository;
import yowyob.comops.api.product.domain.model.ProductPrice;
import java.time.Instant;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ProductPriceR2dbcRepositoryAdapter implements ProductPriceRepository {

    private final ProductPriceSpringDataRepository repository;

    public ProductPriceR2dbcRepositoryAdapter(ProductPriceSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<ProductPrice> save(ProductPrice price) {
        return repository.save(new ProductPriceEntity(price.id(), price.tenantId(), price.createdAt(), price.updatedAt(),
                price.productId(), price.priceType(), price.amount(), price.currency(), price.effectiveFrom()))
                .map(this::toDomain);
    }

    @Override
    public Flux<ProductPrice> findByProductId(UUID tenantId, UUID productId) {
        return repository.findAllByTenantIdAndProductIdOrderByEffectiveFromDesc(tenantId, productId).map(this::toDomain);
    }

    @Override
    public Mono<ProductPrice> findEffectivePrice(UUID tenantId, UUID productId, String priceType, Instant at) {
        return repository
                .findFirstByTenantIdAndProductIdAndPriceTypeIgnoreCaseAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                        tenantId, productId, priceType, at)
                .map(this::toDomain);
    }

    private ProductPrice toDomain(ProductPriceEntity entity) {
        return ProductPrice.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.productId(), entity.priceType(), entity.amount(), entity.currency(), entity.effectiveFrom());
    }
}
