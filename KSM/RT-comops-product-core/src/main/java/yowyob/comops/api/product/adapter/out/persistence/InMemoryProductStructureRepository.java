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
public class InMemoryProductStructureRepository implements ProductStructureRepository {

    private final Map<UUID, CategoryI18n> translations = new ConcurrentHashMap<>();
    private final Map<UUID, ProductSpec> specs = new ConcurrentHashMap<>();
    private final Map<UUID, Variant> variants = new ConcurrentHashMap<>();
    private final Map<UUID, VariantAttribute> attributes = new ConcurrentHashMap<>();
    private final Map<UUID, VariantPrice> prices = new ConcurrentHashMap<>();
    private final Map<UUID, Batch> batches = new ConcurrentHashMap<>();
    private final Map<UUID, MediaAsset> assets = new ConcurrentHashMap<>();

    @Override
    public Mono<CategoryI18n> saveCategoryTranslation(CategoryI18n translation) {
        return Mono.fromSupplier(() -> {
            translations.values().removeIf(existing -> existing.tenantId().equals(translation.tenantId())
                    && existing.categoryId().equals(translation.categoryId())
                    && existing.locale().equalsIgnoreCase(translation.locale()));
            translations.put(translation.id(), translation);
            return translation;
        });
    }

    @Override
    public Mono<CategoryI18n> findCategoryTranslation(UUID tenantId, UUID categoryId, String locale) {
        return Mono.justOrEmpty(translations.values().stream()
                .filter(translation -> translation.tenantId().equals(tenantId))
                .filter(translation -> translation.categoryId().equals(categoryId))
                .filter(translation -> translation.locale().equalsIgnoreCase(locale))
                .findFirst());
    }

    @Override
    public Flux<CategoryI18n> findCategoryTranslations(UUID tenantId, UUID categoryId) {
        return Flux.fromStream(translations.values().stream()
                .filter(translation -> translation.tenantId().equals(tenantId))
                .filter(translation -> translation.categoryId().equals(categoryId)));
    }

    @Override
    public Mono<ProductSpec> saveProductSpec(ProductSpec spec) {
        return Mono.fromSupplier(() -> {
            specs.values().removeIf(existing -> existing.tenantId().equals(spec.tenantId())
                    && existing.productId().equals(spec.productId()));
            specs.put(spec.id(), spec);
            return spec;
        });
    }

    @Override
    public Mono<ProductSpec> findProductSpec(UUID tenantId, UUID productId) {
        return Mono.justOrEmpty(specs.values().stream()
                .filter(spec -> spec.tenantId().equals(tenantId))
                .filter(spec -> spec.productId().equals(productId))
                .findFirst());
    }

    @Override
    public Mono<Boolean> existsVariantSku(UUID tenantId, UUID organizationId, String sku) {
        return Mono.fromSupplier(() -> variants.values().stream()
                .filter(variant -> variant.tenantId().equals(tenantId))
                .anyMatch(variant -> variant.sku().equalsIgnoreCase(sku)));
    }

    @Override
    public Mono<Variant> saveVariant(Variant variant) {
        return Mono.fromSupplier(() -> {
            if (variant.isDefault()) {
                variants.values().removeIf(existing -> existing.tenantId().equals(variant.tenantId())
                        && existing.productId().equals(variant.productId())
                        && existing.isDefault()
                        && !existing.id().equals(variant.id()));
            }
            variants.put(variant.id(), variant);
            return variant;
        });
    }

    @Override
    public Mono<Variant> findVariantById(UUID tenantId, UUID variantId) {
        return Mono.justOrEmpty(variants.get(variantId))
                .filter(variant -> variant.tenantId().equals(tenantId));
    }

    @Override
    public Mono<Variant> findDefaultVariant(UUID tenantId, UUID productId) {
        return Mono.justOrEmpty(variants.values().stream()
                .filter(variant -> variant.tenantId().equals(tenantId))
                .filter(variant -> variant.productId().equals(productId))
                .filter(Variant::isDefault)
                .findFirst());
    }

    @Override
    public Flux<Variant> findVariants(UUID tenantId, UUID productId) {
        return Flux.fromStream(variants.values().stream()
                .filter(variant -> variant.tenantId().equals(tenantId))
                .filter(variant -> variant.productId().equals(productId)));
    }

    @Override
    public Mono<VariantAttribute> saveVariantAttribute(VariantAttribute attribute) {
        return Mono.fromSupplier(() -> {
            attributes.put(attribute.id(), attribute);
            return attribute;
        });
    }

    @Override
    public Flux<VariantAttribute> findVariantAttributes(UUID tenantId, UUID variantId) {
        return Flux.fromStream(attributes.values().stream()
                .filter(attribute -> attribute.tenantId().equals(tenantId))
                .filter(attribute -> attribute.variantId().equals(variantId)));
    }

    @Override
    public Mono<VariantPrice> saveVariantPrice(VariantPrice price) {
        return Mono.fromSupplier(() -> {
            prices.put(price.id(), price);
            return price;
        });
    }

    @Override
    public Flux<VariantPrice> findVariantPrices(UUID tenantId, UUID variantId) {
        return Flux.fromStream(prices.values().stream()
                .filter(price -> price.tenantId().equals(tenantId))
                .filter(price -> price.variantId().equals(variantId))
                .sorted(Comparator.comparing(VariantPrice::effectiveFrom)));
    }

    @Override
    public Mono<VariantPrice> findEffectiveVariantPrice(UUID tenantId, UUID variantId, String priceType, Instant at) {
        return Mono.justOrEmpty(prices.values().stream()
                .filter(price -> price.tenantId().equals(tenantId))
                .filter(price -> price.variantId().equals(variantId))
                .filter(price -> price.priceType().equalsIgnoreCase(priceType))
                .filter(price -> !price.effectiveFrom().isAfter(at))
                .max(Comparator.comparing(VariantPrice::effectiveFrom)));
    }

    @Override
    public Mono<Boolean> existsBatchLotNumber(UUID tenantId, UUID productId, String lotNumber) {
        return Mono.fromSupplier(() -> batches.values().stream()
                .filter(batch -> batch.tenantId().equals(tenantId))
                .filter(batch -> batch.productId().equals(productId))
                .anyMatch(batch -> batch.lotNumber().equalsIgnoreCase(lotNumber)));
    }

    @Override
    public Mono<Batch> saveBatch(Batch batch) {
        return Mono.fromSupplier(() -> {
            batches.put(batch.id(), batch);
            return batch;
        });
    }

    @Override
    public Flux<Batch> findBatches(UUID tenantId, UUID productId) {
        return Flux.fromStream(batches.values().stream()
                .filter(batch -> batch.tenantId().equals(tenantId))
                .filter(batch -> batch.productId().equals(productId)));
    }

    @Override
    public Mono<MediaAsset> saveMediaAsset(MediaAsset mediaAsset) {
        return Mono.fromSupplier(() -> {
            assets.put(mediaAsset.id(), mediaAsset);
            return mediaAsset;
        });
    }

    @Override
    public Flux<MediaAsset> findMediaAssets(UUID tenantId, String targetType, UUID targetId) {
        return Flux.fromStream(assets.values().stream()
                .filter(asset -> asset.tenantId().equals(tenantId))
                .filter(asset -> asset.targetType().equalsIgnoreCase(targetType))
                .filter(asset -> asset.targetId().equals(targetId))
                .sorted(Comparator.comparingInt(MediaAsset::position)));
    }
}
