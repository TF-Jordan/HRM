package yowyob.comops.api.product.application.service;

import yowyob.comops.api.product.application.port.in.ProductCatalogUseCase;
import yowyob.comops.api.product.application.port.out.ProductCategoryRepository;
import yowyob.comops.api.product.application.port.out.ProductPriceRepository;
import yowyob.comops.api.product.application.port.out.ProductRepository;
import yowyob.comops.api.product.domain.DuplicateProductCategoryCodeException;
import yowyob.comops.api.product.domain.ProductNotFoundException;
import yowyob.comops.api.product.domain.model.ProductCategory;
import yowyob.comops.api.product.domain.model.ProductPrice;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductCatalogApplicationService implements ProductCatalogUseCase {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductPriceRepository productPriceRepository;
    private final ProductRepository productRepository;

    public ProductCatalogApplicationService(ProductCategoryRepository productCategoryRepository,
            ProductPriceRepository productPriceRepository, ProductRepository productRepository) {
        this.productCategoryRepository = productCategoryRepository;
        this.productPriceRepository = productPriceRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Mono<ProductCategory> createCategory(UUID tenantId, UUID organizationId, String code, String name,
            String parentCode, String description) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(organizationId, "organizationId is required");
        return productCategoryRepository.existsByCode(tenantId, organizationId, code)
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateProductCategoryCodeException(code))
                        : validateParent(tenantId, organizationId, parentCode)
                                .then(productCategoryRepository.save(ProductCategory.create(tenantId, organizationId,
                                        code, name, parentCode, description))));
    }

    @Override
    public Flux<ProductCategory> listCategories(UUID tenantId, UUID organizationId) {
        return productCategoryRepository.findByOrganizationId(tenantId, organizationId);
    }

    @Override
    public Mono<ProductPrice> definePrice(UUID tenantId, UUID productId, String priceType, BigDecimal amount,
            String currency, Instant effectiveFrom) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(productId, "productId is required");
        return productRepository.findById(tenantId, productId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                .then(productPriceRepository.save(ProductPrice.create(tenantId, productId, priceType, amount, currency,
                        effectiveFrom == null ? Instant.now() : effectiveFrom)));
    }

    @Override
    public Flux<ProductPrice> listPrices(UUID tenantId, UUID productId) {
        return productPriceRepository.findByProductId(tenantId, productId);
    }

    @Override
    public Mono<ProductPrice> resolveEffectivePrice(UUID tenantId, UUID productId, String priceType, Instant at) {
        return productPriceRepository.findEffectivePrice(tenantId, productId, priceType, at == null ? Instant.now() : at);
    }

    private Mono<Void> validateParent(UUID tenantId, UUID organizationId, String parentCode) {
        if (parentCode == null || parentCode.isBlank()) {
            return Mono.empty();
        }
        return productCategoryRepository.findByCode(tenantId, organizationId, parentCode)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("parent category not found")))
                .then();
    }
}
