package yowyob.comops.api.product.application.port.out;

import yowyob.comops.api.product.domain.model.Batch;
import yowyob.comops.api.product.domain.model.CategoryI18n;
import yowyob.comops.api.product.domain.model.MediaAsset;
import yowyob.comops.api.product.domain.model.ProductSpec;
import yowyob.comops.api.product.domain.model.Variant;
import yowyob.comops.api.product.domain.model.VariantAttribute;
import yowyob.comops.api.product.domain.model.VariantPrice;
import java.time.Instant;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductStructureRepository {

    Mono<CategoryI18n> saveCategoryTranslation(CategoryI18n translation);

    Mono<CategoryI18n> findCategoryTranslation(UUID tenantId, UUID categoryId, String locale);

    Flux<CategoryI18n> findCategoryTranslations(UUID tenantId, UUID categoryId);

    Mono<ProductSpec> saveProductSpec(ProductSpec spec);

    Mono<ProductSpec> findProductSpec(UUID tenantId, UUID productId);

    Mono<Boolean> existsVariantSku(UUID tenantId, UUID organizationId, String sku);

    Mono<Variant> saveVariant(Variant variant);

    Mono<Variant> findVariantById(UUID tenantId, UUID variantId);

    Mono<Variant> findDefaultVariant(UUID tenantId, UUID productId);

    Flux<Variant> findVariants(UUID tenantId, UUID productId);

    Mono<VariantAttribute> saveVariantAttribute(VariantAttribute attribute);

    Flux<VariantAttribute> findVariantAttributes(UUID tenantId, UUID variantId);

    Mono<VariantPrice> saveVariantPrice(VariantPrice price);

    Flux<VariantPrice> findVariantPrices(UUID tenantId, UUID variantId);

    Mono<VariantPrice> findEffectiveVariantPrice(UUID tenantId, UUID variantId, String priceType, Instant at);

    Mono<Boolean> existsBatchLotNumber(UUID tenantId, UUID productId, String lotNumber);

    Mono<Batch> saveBatch(Batch batch);

    Flux<Batch> findBatches(UUID tenantId, UUID productId);

    Mono<MediaAsset> saveMediaAsset(MediaAsset mediaAsset);

    Flux<MediaAsset> findMediaAssets(UUID tenantId, String targetType, UUID targetId);
}
