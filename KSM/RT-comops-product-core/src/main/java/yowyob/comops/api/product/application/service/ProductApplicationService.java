package yowyob.comops.api.product.application.service;

import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.product.application.port.in.CreateProductCommand;
import yowyob.comops.api.product.application.port.in.CreateProductUseCase;
import yowyob.comops.api.product.application.port.in.DeleteProductUseCase;
import yowyob.comops.api.product.application.port.in.GetProductUseCase;
import yowyob.comops.api.product.application.port.in.ListProductsUseCase;
import yowyob.comops.api.product.application.port.in.SearchProductsUseCase;
import yowyob.comops.api.product.application.port.in.UpdateProductCommand;
import yowyob.comops.api.product.application.port.in.UpdateProductUseCase;
import yowyob.comops.api.product.application.port.out.ProductRepository;
import yowyob.comops.api.product.application.port.out.ProductSearchGateway;
import yowyob.comops.api.product.application.port.out.ProductStructureRepository;
import yowyob.comops.api.product.domain.DuplicateProductSkuException;
import yowyob.comops.api.product.domain.ProductNotFoundException;
import yowyob.comops.api.product.domain.ProductSearchUnavailableException;
import yowyob.comops.api.product.domain.model.Product;
import yowyob.comops.api.product.domain.model.ProductSearchResult;
import yowyob.comops.api.product.domain.model.Variant;
import yowyob.comops.api.product.domain.model.VariantPrice;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@Service
public class ProductApplicationService implements CreateProductUseCase, GetProductUseCase, ListProductsUseCase,
        SearchProductsUseCase, UpdateProductUseCase, DeleteProductUseCase {

    private final ProductRepository productRepository;
    private final ProductStructureRepository productStructureRepository;
    private final Optional<ProductSearchGateway> productSearchGateway;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public ProductApplicationService(ProductRepository productRepository, BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor, Optional<ProductSearchGateway> productSearchGateway,
            ProductStructureRepository productStructureRepository) {
        this.productRepository = productRepository;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
        this.productSearchGateway = productSearchGateway;
        this.productStructureRepository = productStructureRepository;
    }

    @Override
    public Mono<Product> createProduct(CreateProductCommand command) {
        Objects.requireNonNull(command, "command is required");
        Product product = Product.create(command.tenantId(), command.organizationId(), command.sku(), command.name(),
                command.familyCode(), command.categoryCode(), command.variantLabel(), command.barcode(), command.description(),
                command.unitPrice(), command.currency(), command.status());
        Mono<Product> operation = productRepository.existsBySku(product.tenantId(), product.organizationId(), product.sku())
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateProductSkuException(product.sku()))
                        : productRepository.save(product)
                                .flatMap(saved -> syncDefaultVariant(saved)
                                        .then(businessEventPublisher.publish(productCreatedEvent(saved)))
                                        .thenReturn(saved)));
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<Product> getProduct(java.util.UUID productId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> productRepository.findById(context.tenantId(), productId))
                .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)));
    }

    @Override
    public Flux<Product> listProducts(java.util.UUID organizationId, String familyCode, String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> productRepository.findByOrganizationId(context.tenantId(), organizationId))
                .filter(product -> familyCode == null || familyCode.isBlank()
                        || product.familyCode().equalsIgnoreCase(familyCode))
                .filter(product -> status == null || status.isBlank()
                        || product.status().equalsIgnoreCase(status));
    }

    @Override
    public Flux<ProductSearchResult> searchProducts(java.util.UUID organizationId, String query, String familyCode,
            String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> productSearchGateway
                        .map(gateway -> gateway.search(context.tenantId(), organizationId, query, familyCode, status))
                        .orElseGet(() -> Flux.error(new ProductSearchUnavailableException())));
    }

    @Override
    public Mono<Product> updateProduct(UpdateProductCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<Product> operation = productRepository.findById(command.tenantId(), command.productId())
                .switchIfEmpty(Mono.error(new ProductNotFoundException(command.productId())))
                .flatMap(existing -> {
                    if (!existing.organizationId().equals(command.organizationId())) {
                        return Mono.error(new IllegalArgumentException("product does not belong to the provided organization"));
                    }
                            Product updated = existing.update(command.sku(), command.name(), command.familyCode(),
                            command.categoryCode(), command.variantLabel(), command.barcode(), command.description(), command.unitPrice(),
                            command.currency(), command.status());
                    return productRepository.existsBySkuExcludingId(updated.tenantId(), updated.organizationId(),
                                    updated.sku(), updated.id())
                            .flatMap(exists -> exists
                                    ? Mono.error(new DuplicateProductSkuException(updated.sku()))
                                    : productRepository.save(updated)
                                            .flatMap(saved -> syncDefaultVariant(saved).thenReturn(saved)));
                });
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<Void> deleteProduct(java.util.UUID productId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> productRepository.findById(context.tenantId(), productId)
                        .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                        .then(productRepository.deleteById(context.tenantId(), productId)))
                .as(transactionalExecutor::transactional);
    }

    private BusinessEvent productCreatedEvent(Product product) {
        return BusinessEvent.now(product.tenantId(), product.organizationId(), "PRODUCT_CREATED", "PRODUCT",
                product.id(), payload(
                        "sku", product.sku(),
                        "name", product.name(),
                        "familyCode", product.familyCode(),
                        "categoryCode", product.categoryCode(),
                        "variantLabel", product.variantLabel(),
                        "barcode", product.barcode(),
                        "description", product.description(),
                        "unitPrice", product.unitPrice(),
                        "currency", product.currency(),
                        "status", product.status()));
    }

    private Mono<Void> syncDefaultVariant(Product product) {
        return productStructureRepository.findDefaultVariant(product.tenantId(), product.id())
                .switchIfEmpty(Mono.defer(() -> productStructureRepository.saveVariant(Variant.create(product.tenantId(),
                        product.id(), product.sku(), product.barcode(), product.variantLabel(), true, product.status()))))
                .cast(Variant.class)
                .flatMap(existing -> {
                    Variant variant = existing.id() != null && existing.productId().equals(product.id())
                            ? existing.update(product.sku(), product.barcode(), product.variantLabel(), true, product.status())
                            : Variant.create(product.tenantId(), product.id(), product.sku(), product.barcode(),
                                    product.variantLabel(), true, product.status());
                    return productStructureRepository.saveVariant(variant)
                            .flatMap(savedVariant -> productStructureRepository.saveVariantPrice(VariantPrice.create(
                                            product.tenantId(), savedVariant.id(), "SALE", product.unitPrice(),
                                            product.currency(), product.updatedAt()))
                                    .then());
                });
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
