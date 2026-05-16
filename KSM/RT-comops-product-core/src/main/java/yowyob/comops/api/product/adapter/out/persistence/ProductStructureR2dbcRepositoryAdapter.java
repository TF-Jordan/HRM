package yowyob.comops.api.product.adapter.out.persistence;

import yowyob.comops.api.product.application.port.out.ProductStructureRepository;
import yowyob.comops.api.product.domain.model.Batch;
import yowyob.comops.api.product.domain.model.CategoryI18n;
import yowyob.comops.api.product.domain.model.MediaAsset;
import yowyob.comops.api.product.domain.model.ProductSpec;
import yowyob.comops.api.product.domain.model.Variant;
import yowyob.comops.api.product.domain.model.VariantAttribute;
import yowyob.comops.api.product.domain.model.VariantPrice;
import java.time.Instant;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ProductStructureR2dbcRepositoryAdapter implements ProductStructureRepository {

    private final CategoryI18nSpringDataRepository categoryI18nRepository;
    private final ProductSpecSpringDataRepository productSpecRepository;
    private final VariantSpringDataRepository variantRepository;
    private final VariantAttributeSpringDataRepository variantAttributeRepository;
    private final VariantPriceSpringDataRepository variantPriceRepository;
    private final BatchSpringDataRepository batchRepository;
    private final MediaAssetSpringDataRepository mediaAssetRepository;

    public ProductStructureR2dbcRepositoryAdapter(CategoryI18nSpringDataRepository categoryI18nRepository,
            ProductSpecSpringDataRepository productSpecRepository, VariantSpringDataRepository variantRepository,
            VariantAttributeSpringDataRepository variantAttributeRepository,
            VariantPriceSpringDataRepository variantPriceRepository, BatchSpringDataRepository batchRepository,
            MediaAssetSpringDataRepository mediaAssetRepository) {
        this.categoryI18nRepository = categoryI18nRepository;
        this.productSpecRepository = productSpecRepository;
        this.variantRepository = variantRepository;
        this.variantAttributeRepository = variantAttributeRepository;
        this.variantPriceRepository = variantPriceRepository;
        this.batchRepository = batchRepository;
        this.mediaAssetRepository = mediaAssetRepository;
    }

    @Override
    public Mono<CategoryI18n> saveCategoryTranslation(CategoryI18n translation) {
        CategoryI18nEntity entity = new CategoryI18nEntity(translation.id(), translation.tenantId(), translation.createdAt(),
                translation.updatedAt(), translation.categoryId(), translation.locale(), translation.name(),
                translation.description());
        return categoryI18nRepository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<CategoryI18n> findCategoryTranslation(UUID tenantId, UUID categoryId, String locale) {
        return categoryI18nRepository.findByTenantIdAndCategoryIdAndLocale(tenantId, categoryId, locale.toLowerCase())
                .map(this::toDomain);
    }

    @Override
    public Flux<CategoryI18n> findCategoryTranslations(UUID tenantId, UUID categoryId) {
        return categoryI18nRepository.findAllByTenantIdAndCategoryId(tenantId, categoryId).map(this::toDomain);
    }

    @Override
    public Mono<ProductSpec> saveProductSpec(ProductSpec spec) {
        ProductSpecEntity entity = new ProductSpecEntity(spec.id(), spec.tenantId(), spec.createdAt(), spec.updatedAt(),
                spec.productId(), spec.weightKg(), spec.lengthCm(), spec.widthCm(), spec.heightCm(), spec.materials());
        return productSpecRepository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<ProductSpec> findProductSpec(UUID tenantId, UUID productId) {
        return productSpecRepository.findByTenantIdAndProductId(tenantId, productId).map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsVariantSku(UUID tenantId, UUID organizationId, String sku) {
        return variantRepository.existsByTenantIdAndSkuIgnoreCase(tenantId, sku);
    }

    @Override
    public Mono<Variant> saveVariant(Variant variant) {
        VariantEntity entity = new VariantEntity(variant.id(), variant.tenantId(), variant.createdAt(), variant.updatedAt(),
                variant.productId(), variant.sku(), variant.barcode(), variant.label(), variant.isDefault(),
                variant.status());
        return variantRepository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<Variant> findVariantById(UUID tenantId, UUID variantId) {
        return variantRepository.findByIdAndTenantId(variantId, tenantId).map(this::toDomain);
    }

    @Override
    public Mono<Variant> findDefaultVariant(UUID tenantId, UUID productId) {
        return variantRepository.findByTenantIdAndProductIdAndIsDefaultTrue(tenantId, productId).map(this::toDomain);
    }

    @Override
    public Flux<Variant> findVariants(UUID tenantId, UUID productId) {
        return variantRepository.findAllByTenantIdAndProductId(tenantId, productId).map(this::toDomain);
    }

    @Override
    public Mono<VariantAttribute> saveVariantAttribute(VariantAttribute attribute) {
        VariantAttributeEntity entity = new VariantAttributeEntity(attribute.id(), attribute.tenantId(), attribute.createdAt(),
                attribute.updatedAt(), attribute.variantId(), attribute.attributeName(), attribute.attributeValue());
        return variantAttributeRepository.save(entity).map(this::toDomain);
    }

    @Override
    public Flux<VariantAttribute> findVariantAttributes(UUID tenantId, UUID variantId) {
        return variantAttributeRepository.findAllByTenantIdAndVariantId(tenantId, variantId).map(this::toDomain);
    }

    @Override
    public Mono<VariantPrice> saveVariantPrice(VariantPrice price) {
        VariantPriceEntity entity = new VariantPriceEntity(price.id(), price.tenantId(), price.createdAt(), price.updatedAt(),
                price.variantId(), price.priceType(), price.amount(), price.currency(), price.effectiveFrom());
        return variantPriceRepository.save(entity).map(this::toDomain);
    }

    @Override
    public Flux<VariantPrice> findVariantPrices(UUID tenantId, UUID variantId) {
        return variantPriceRepository.findAllByTenantIdAndVariantId(tenantId, variantId).map(this::toDomain);
    }

    @Override
    public Mono<VariantPrice> findEffectiveVariantPrice(UUID tenantId, UUID variantId, String priceType, Instant at) {
        return variantPriceRepository
                .findFirstByTenantIdAndVariantIdAndPriceTypeIgnoreCaseAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                        tenantId, variantId, priceType, at)
                .map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsBatchLotNumber(UUID tenantId, UUID productId, String lotNumber) {
        return batchRepository.existsByTenantIdAndProductIdAndLotNumberIgnoreCase(tenantId, productId, lotNumber);
    }

    @Override
    public Mono<Batch> saveBatch(Batch batch) {
        BatchEntity entity = new BatchEntity(batch.id(), batch.tenantId(), batch.createdAt(), batch.updatedAt(),
                batch.productId(), batch.lotNumber(), batch.manufacturingDate(), batch.expiryDate(), batch.quantity());
        return batchRepository.save(entity).map(this::toDomain);
    }

    @Override
    public Flux<Batch> findBatches(UUID tenantId, UUID productId) {
        return batchRepository.findAllByTenantIdAndProductId(tenantId, productId).map(this::toDomain);
    }

    @Override
    public Mono<MediaAsset> saveMediaAsset(MediaAsset mediaAsset) {
        MediaAssetEntity entity = new MediaAssetEntity(mediaAsset.id(), mediaAsset.tenantId(), mediaAsset.createdAt(),
                mediaAsset.updatedAt(), mediaAsset.targetType(), mediaAsset.targetId(), mediaAsset.fileId(),
                mediaAsset.mimeType(), mediaAsset.position(), mediaAsset.altText());
        return mediaAssetRepository.save(entity).map(this::toDomain);
    }

    @Override
    public Flux<MediaAsset> findMediaAssets(UUID tenantId, String targetType, UUID targetId) {
        return mediaAssetRepository.findAllByTenantIdAndTargetTypeAndTargetIdOrderByPositionAsc(tenantId,
                targetType.toUpperCase(), targetId).map(this::toDomain);
    }

    private CategoryI18n toDomain(CategoryI18nEntity entity) {
        return CategoryI18n.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.categoryId(), entity.locale(), entity.name(), entity.description());
    }

    private ProductSpec toDomain(ProductSpecEntity entity) {
        return ProductSpec.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.productId(), entity.weightKg(), entity.lengthCm(), entity.widthCm(), entity.heightCm(),
                entity.materials());
    }

    private Variant toDomain(VariantEntity entity) {
        return Variant.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(), entity.productId(),
                entity.sku(), entity.barcode(), entity.label(), entity.isDefault(), entity.status());
    }

    private VariantAttribute toDomain(VariantAttributeEntity entity) {
        return VariantAttribute.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.variantId(), entity.attributeName(), entity.attributeValue());
    }

    private VariantPrice toDomain(VariantPriceEntity entity) {
        return VariantPrice.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.variantId(), entity.priceType(), entity.amount(), entity.currency(), entity.effectiveFrom());
    }

    private Batch toDomain(BatchEntity entity) {
        return Batch.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(), entity.productId(),
                entity.lotNumber(), entity.manufacturingDate(), entity.expiryDate(), entity.quantity());
    }

    private MediaAsset toDomain(MediaAssetEntity entity) {
        return MediaAsset.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.targetType(), entity.targetId(), entity.fileId(), entity.mimeType(), entity.position(),
                entity.altText());
    }
}
