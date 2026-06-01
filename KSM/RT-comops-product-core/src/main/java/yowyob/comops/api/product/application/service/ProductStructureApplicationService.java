package yowyob.comops.api.product.application.service;

import yowyob.comops.api.product.application.port.in.ProductStructureUseCase;
import yowyob.comops.api.product.application.port.out.ProductCategoryRepository;
import yowyob.comops.api.product.application.port.out.ProductRepository;
import yowyob.comops.api.product.application.port.out.ProductStructureRepository;
import yowyob.comops.api.product.domain.DuplicateBatchLotNumberException;
import yowyob.comops.api.product.domain.DuplicateVariantSkuException;
import yowyob.comops.api.product.domain.ProductNotFoundException;
import yowyob.comops.api.product.domain.model.Batch;
import yowyob.comops.api.product.domain.model.CategoryI18n;
import yowyob.comops.api.product.domain.model.MediaAsset;
import yowyob.comops.api.product.domain.model.ProductSpec;
import yowyob.comops.api.product.domain.model.Variant;
import yowyob.comops.api.product.domain.model.VariantAttribute;
import yowyob.comops.api.product.domain.model.VariantPrice;
import yowyob.comops.api.file.application.port.out.StoredFileRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductStructureApplicationService implements ProductStructureUseCase {

    private final ProductStructureRepository productStructureRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final StoredFileRepository storedFileRepository;

    public ProductStructureApplicationService(ProductStructureRepository productStructureRepository,
            ProductRepository productRepository, ProductCategoryRepository productCategoryRepository,
            StoredFileRepository storedFileRepository) {
        this.productStructureRepository = productStructureRepository;
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.storedFileRepository = storedFileRepository;
    }

    @Override
    public Mono<CategoryI18n> upsertCategoryTranslation(UUID tenantId, UUID categoryId, String locale, String name,
            String description) {
        return productCategoryRepository.findById(tenantId, categoryId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("category not found")))
                .then(productStructureRepository.findCategoryTranslation(tenantId, categoryId, locale)
                        .map(existing -> CategoryI18n.rehydrate(existing.id(), existing.tenantId(), existing.createdAt(),
                                Instant.now(), existing.categoryId(), locale, name, description))
                        .switchIfEmpty(Mono.fromSupplier(() -> CategoryI18n.create(tenantId, categoryId, locale, name,
                                description))))
                .flatMap(productStructureRepository::saveCategoryTranslation);
    }

    @Override
    public Flux<CategoryI18n> listCategoryTranslations(UUID tenantId, UUID categoryId) {
        return productStructureRepository.findCategoryTranslations(tenantId, categoryId);
    }

    @Override
    public Mono<ProductSpec> upsertProductSpec(UUID tenantId, UUID productId, BigDecimal weightKg, BigDecimal lengthCm,
            BigDecimal widthCm, BigDecimal heightCm, String materials) {
        return productRepository.findById(tenantId, productId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                .then(productStructureRepository.findProductSpec(tenantId, productId)
                        .map(existing -> existing.update(weightKg, lengthCm, widthCm, heightCm, materials))
                        .switchIfEmpty(Mono.fromSupplier(() -> ProductSpec.create(tenantId, productId, weightKg,
                                lengthCm, widthCm, heightCm, materials))))
                .flatMap(productStructureRepository::saveProductSpec);
    }

    @Override
    public Mono<ProductSpec> getProductSpec(UUID tenantId, UUID productId) {
        return productStructureRepository.findProductSpec(tenantId, productId);
    }

    @Override
    public Mono<Variant> createVariant(UUID tenantId, UUID productId, String sku, String barcode, String label,
            boolean isDefault, String status) {
        return productRepository.findById(tenantId, productId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                .flatMap(product -> productStructureRepository.existsVariantSku(tenantId, product.organizationId(), sku)
                        .flatMap(exists -> exists
                                ? Mono.error(new DuplicateVariantSkuException(sku))
                                : validateDefaultVariant(tenantId, productId, isDefault)
                                        .then(productStructureRepository.saveVariant(Variant.create(tenantId, productId,
                                                sku, barcode, label, isDefault, status)))));
    }

    @Override
    public Flux<Variant> listVariants(UUID tenantId, UUID productId) {
        return productStructureRepository.findVariants(tenantId, productId);
    }

    @Override
    public Mono<VariantAttribute> addVariantAttribute(UUID tenantId, UUID variantId, String attributeName,
            String attributeValue) {
        return productStructureRepository.findVariantById(tenantId, variantId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("variant not found")))
                .then(productStructureRepository.saveVariantAttribute(
                        VariantAttribute.create(tenantId, variantId, attributeName, attributeValue)));
    }

    @Override
    public Flux<VariantAttribute> listVariantAttributes(UUID tenantId, UUID variantId) {
        return productStructureRepository.findVariantAttributes(tenantId, variantId);
    }

    @Override
    public Mono<VariantPrice> defineVariantPrice(UUID tenantId, UUID variantId, String priceType, BigDecimal amount,
            String currency, Instant effectiveFrom) {
        return productStructureRepository.findVariantById(tenantId, variantId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("variant not found")))
                .then(productStructureRepository.saveVariantPrice(
                        VariantPrice.create(tenantId, variantId, priceType, amount, currency, effectiveFrom)));
    }

    @Override
    public Flux<VariantPrice> listVariantPrices(UUID tenantId, UUID variantId) {
        return productStructureRepository.findVariantPrices(tenantId, variantId);
    }

    @Override
    public Mono<VariantPrice> resolveEffectiveVariantPrice(UUID tenantId, UUID variantId, String priceType, Instant at) {
        return productStructureRepository.findEffectiveVariantPrice(tenantId, variantId, priceType,
                at == null ? Instant.now() : at);
    }

    @Override
    public Mono<Batch> createBatch(UUID tenantId, UUID productId, String lotNumber, LocalDate manufacturingDate,
            LocalDate expiryDate, int quantity) {
        return productRepository.findById(tenantId, productId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                .then(productStructureRepository.existsBatchLotNumber(tenantId, productId, lotNumber)
                        .flatMap(exists -> exists
                                ? Mono.error(new DuplicateBatchLotNumberException(lotNumber))
                                : productStructureRepository.saveBatch(Batch.create(tenantId, productId, lotNumber,
                                        manufacturingDate, expiryDate, quantity))));
    }

    @Override
    public Flux<Batch> listBatches(UUID tenantId, UUID productId) {
        return productStructureRepository.findBatches(tenantId, productId);
    }

    @Override
    public Mono<MediaAsset> createMediaAsset(UUID tenantId, String targetType, UUID targetId, UUID fileId,
            String mimeType, int position, String altText) {
        return validateMediaTarget(tenantId, targetType, targetId)
                .then(validateStoredFile(tenantId, fileId))
                .then(productStructureRepository.saveMediaAsset(MediaAsset.create(tenantId, targetType, targetId, fileId,
                        mimeType, position, altText)));
    }

    @Override
    public Flux<MediaAsset> listMediaAssets(UUID tenantId, String targetType, UUID targetId) {
        return productStructureRepository.findMediaAssets(tenantId, targetType, targetId);
    }

    private Mono<Void> validateDefaultVariant(UUID tenantId, UUID productId, boolean isDefault) {
        if (!isDefault) {
            return Mono.empty();
        }
        return productStructureRepository.findDefaultVariant(tenantId, productId)
                .flatMap(existing -> Mono.error(new IllegalArgumentException("default variant already exists")))
                .then();
    }

    private Mono<Void> validateMediaTarget(UUID tenantId, String targetType, UUID targetId) {
        if ("PRODUCT".equalsIgnoreCase(targetType)) {
            return productRepository.findById(tenantId, targetId)
                    .switchIfEmpty(Mono.error(new ProductNotFoundException(targetId)))
                    .then();
        }
        if ("VARIANT".equalsIgnoreCase(targetType)) {
            return productStructureRepository.findVariantById(tenantId, targetId)
                    .switchIfEmpty(Mono.error(new IllegalArgumentException("variant not found")))
                    .then();
        }
        return Mono.error(new IllegalArgumentException("targetType must be PRODUCT or VARIANT"));
    }

    private Mono<Void> validateStoredFile(UUID tenantId, UUID fileId) {
        return storedFileRepository.findById(tenantId, fileId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("fileId does not reference an existing file")))
                .then();
    }
}
