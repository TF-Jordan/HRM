package yowyob.comops.api.product.application.port.in;

import yowyob.comops.api.product.domain.model.Batch;
import yowyob.comops.api.product.domain.model.CategoryI18n;
import yowyob.comops.api.product.domain.model.MediaAsset;
import yowyob.comops.api.product.domain.model.ProductSpec;
import yowyob.comops.api.product.domain.model.Variant;
import yowyob.comops.api.product.domain.model.VariantAttribute;
import yowyob.comops.api.product.domain.model.VariantPrice;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductStructureUseCase {

    Mono<CategoryI18n> upsertCategoryTranslation(UUID tenantId, UUID categoryId, String locale, String name,
            String description);

    Flux<CategoryI18n> listCategoryTranslations(UUID tenantId, UUID categoryId);

    Mono<ProductSpec> upsertProductSpec(UUID tenantId, UUID productId, BigDecimal weightKg, BigDecimal lengthCm,
            BigDecimal widthCm, BigDecimal heightCm, String materials);

    Mono<ProductSpec> getProductSpec(UUID tenantId, UUID productId);

    Mono<Variant> createVariant(UUID tenantId, UUID productId, String sku, String barcode, String label,
            boolean isDefault, String status);

    Flux<Variant> listVariants(UUID tenantId, UUID productId);

    Mono<VariantAttribute> addVariantAttribute(UUID tenantId, UUID variantId, String attributeName, String attributeValue);

    Flux<VariantAttribute> listVariantAttributes(UUID tenantId, UUID variantId);

    Mono<VariantPrice> defineVariantPrice(UUID tenantId, UUID variantId, String priceType, BigDecimal amount,
            String currency, Instant effectiveFrom);

    Flux<VariantPrice> listVariantPrices(UUID tenantId, UUID variantId);

    Mono<VariantPrice> resolveEffectiveVariantPrice(UUID tenantId, UUID variantId, String priceType, Instant at);

    Mono<Batch> createBatch(UUID tenantId, UUID productId, String lotNumber, LocalDate manufacturingDate,
            LocalDate expiryDate, int quantity);

    Flux<Batch> listBatches(UUID tenantId, UUID productId);

    Mono<MediaAsset> createMediaAsset(UUID tenantId, String targetType, UUID targetId, UUID fileId, String mimeType,
            int position, String altText);

    Flux<MediaAsset> listMediaAssets(UUID tenantId, String targetType, UUID targetId);
}
