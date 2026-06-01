package yowyob.comops.api.product.application.port.in;

import yowyob.comops.api.product.domain.model.ProductCategory;
import yowyob.comops.api.product.domain.model.ProductPrice;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductCatalogUseCase {

    Mono<ProductCategory> createCategory(UUID tenantId, UUID organizationId, String code, String name, String parentCode,
            String description);

    Flux<ProductCategory> listCategories(UUID tenantId, UUID organizationId);

    Mono<ProductPrice> definePrice(UUID tenantId, UUID productId, String priceType, BigDecimal amount, String currency,
            Instant effectiveFrom);

    Flux<ProductPrice> listPrices(UUID tenantId, UUID productId);

    Mono<ProductPrice> resolveEffectivePrice(UUID tenantId, UUID productId, String priceType, Instant at);
}
