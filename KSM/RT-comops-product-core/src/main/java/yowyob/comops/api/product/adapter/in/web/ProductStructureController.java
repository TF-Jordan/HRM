package yowyob.comops.api.product.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.product.application.port.in.ProductStructureUseCase;
import yowyob.comops.api.product.domain.model.Batch;
import yowyob.comops.api.product.domain.model.CategoryI18n;
import yowyob.comops.api.product.domain.model.MediaAsset;
import yowyob.comops.api.product.domain.model.ProductSpec;
import yowyob.comops.api.product.domain.model.Variant;
import yowyob.comops.api.product.domain.model.VariantAttribute;
import yowyob.comops.api.product.domain.model.VariantPrice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'products:write')")
public class ProductStructureController {

    private final ProductStructureUseCase useCase;

    public ProductStructureController(ProductStructureUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/product-categories/{categoryId}/translations")
    public Mono<ResponseEntity<ApiResponse<CategoryI18nResponse>>> upsertCategoryTranslation(
            @PathVariable("categoryId") UUID categoryId,
            @Valid @RequestBody Mono<UpsertCategoryTranslationRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.upsertCategoryTranslation(tuple.getT2().tenantId(), categoryId,
                        tuple.getT1().locale(), tuple.getT1().name(), tuple.getT1().description()))
                .map(CategoryI18nResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Category translation saved.")));
    }

    @GetMapping("/product-categories/{categoryId}/translations")
    public Mono<ResponseEntity<ApiResponse<List<CategoryI18nResponse>>>> listCategoryTranslations(
            @PathVariable("categoryId") UUID categoryId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.listCategoryTranslations(context.tenantId(), categoryId)
                        .map(CategoryI18nResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Category translations retrieved.")));
    }

    @PutMapping("/products/{productId}/spec")
    public Mono<ResponseEntity<ApiResponse<ProductSpecResponse>>> upsertProductSpec(
            @PathVariable("productId") UUID productId,
            @Valid @RequestBody Mono<UpsertProductSpecRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.upsertProductSpec(tuple.getT2().tenantId(), productId, tuple.getT1().weightKg(),
                        tuple.getT1().lengthCm(), tuple.getT1().widthCm(), tuple.getT1().heightCm(),
                        tuple.getT1().materials()))
                .map(ProductSpecResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Product specification saved.")));
    }

    @GetMapping("/products/{productId}/spec")
    public Mono<ResponseEntity<ApiResponse<ProductSpecResponse>>> getProductSpec(@PathVariable("productId") UUID productId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.getProductSpec(context.tenantId(), productId))
                .map(ProductSpecResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Product specification retrieved.")));
    }

    @PostMapping("/products/{productId}/variants")
    public Mono<ResponseEntity<ApiResponse<VariantResponse>>> createVariant(
            @PathVariable("productId") UUID productId,
            @Valid @RequestBody Mono<CreateVariantRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.createVariant(tuple.getT2().tenantId(), productId, tuple.getT1().sku(),
                        tuple.getT1().barcode(), tuple.getT1().label(), tuple.getT1().isDefault(),
                        tuple.getT1().status()))
                .map(VariantResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Variant created.")));
    }

    @GetMapping("/products/{productId}/variants")
    public Mono<ResponseEntity<ApiResponse<List<VariantResponse>>>> listVariants(
            @PathVariable("productId") UUID productId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.listVariants(context.tenantId(), productId)
                        .map(VariantResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Variants retrieved.")));
    }

    @PostMapping("/variants/{variantId}/attributes")
    public Mono<ResponseEntity<ApiResponse<VariantAttributeResponse>>> addVariantAttribute(
            @PathVariable("variantId") UUID variantId,
            @Valid @RequestBody Mono<AddVariantAttributeRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.addVariantAttribute(tuple.getT2().tenantId(), variantId,
                        tuple.getT1().attributeName(), tuple.getT1().attributeValue()))
                .map(VariantAttributeResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Variant attribute created.")));
    }

    @GetMapping("/variants/{variantId}/attributes")
    public Mono<ResponseEntity<ApiResponse<List<VariantAttributeResponse>>>> listVariantAttributes(
            @PathVariable("variantId") UUID variantId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.listVariantAttributes(context.tenantId(), variantId)
                        .map(VariantAttributeResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Variant attributes retrieved.")));
    }

    @PostMapping("/variants/{variantId}/prices")
    public Mono<ResponseEntity<ApiResponse<VariantPriceResponse>>> defineVariantPrice(
            @PathVariable("variantId") UUID variantId,
            @Valid @RequestBody Mono<DefineVariantPriceRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.defineVariantPrice(tuple.getT2().tenantId(), variantId, tuple.getT1().priceType(),
                        tuple.getT1().amount(), tuple.getT1().currency(), tuple.getT1().effectiveFrom()))
                .map(VariantPriceResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Variant price created.")));
    }

    @GetMapping("/variants/{variantId}/prices")
    public Mono<ResponseEntity<ApiResponse<List<VariantPriceResponse>>>> listVariantPrices(
            @PathVariable("variantId") UUID variantId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.listVariantPrices(context.tenantId(), variantId)
                        .map(VariantPriceResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Variant prices retrieved.")));
    }

    @GetMapping("/variants/{variantId}/prices/effective")
    public Mono<ResponseEntity<ApiResponse<VariantPriceResponse>>> resolveEffectiveVariantPrice(
            @PathVariable("variantId") UUID variantId,
            @RequestParam("priceType") String priceType,
            @RequestParam(value = "at", required = false) Instant at) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.resolveEffectiveVariantPrice(context.tenantId(), variantId, priceType, at))
                .map(VariantPriceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Effective variant price retrieved.")));
    }

    @PostMapping("/products/{productId}/batches")
    public Mono<ResponseEntity<ApiResponse<BatchResponse>>> createBatch(
            @PathVariable("productId") UUID productId,
            @Valid @RequestBody Mono<CreateBatchRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.createBatch(tuple.getT2().tenantId(), productId, tuple.getT1().lotNumber(),
                        tuple.getT1().manufacturingDate(), tuple.getT1().expiryDate(), tuple.getT1().quantity()))
                .map(BatchResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Batch created.")));
    }

    @GetMapping("/products/{productId}/batches")
    public Mono<ResponseEntity<ApiResponse<List<BatchResponse>>>> listBatches(
            @PathVariable("productId") UUID productId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.listBatches(context.tenantId(), productId)
                        .map(BatchResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Batches retrieved.")));
    }

    @PostMapping("/media-assets")
    public Mono<ResponseEntity<ApiResponse<MediaAssetResponse>>> createMediaAsset(
            @Valid @RequestBody Mono<CreateMediaAssetRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.createMediaAsset(tuple.getT2().tenantId(), tuple.getT1().targetType(),
                        tuple.getT1().targetId(), tuple.getT1().fileId(), tuple.getT1().mimeType(),
                        tuple.getT1().position(), tuple.getT1().altText()))
                .map(MediaAssetResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Media asset created.")));
    }

    @GetMapping("/media-assets")
    public Mono<ResponseEntity<ApiResponse<List<MediaAssetResponse>>>> listMediaAssets(
            @RequestParam("targetType") String targetType,
            @RequestParam("targetId") UUID targetId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.listMediaAssets(context.tenantId(), targetType, targetId)
                        .map(MediaAssetResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Media assets retrieved.")));
    }

    public record UpsertCategoryTranslationRequest(@NotBlank String locale, @NotBlank String name, String description) {
    }

    public record CategoryI18nResponse(UUID id, UUID categoryId, String locale, String name, String description) {
        static CategoryI18nResponse from(CategoryI18n translation) {
            return new CategoryI18nResponse(translation.id(), translation.categoryId(), translation.locale(),
                    translation.name(), translation.description());
        }
    }

    public record UpsertProductSpecRequest(BigDecimal weightKg, BigDecimal lengthCm, BigDecimal widthCm,
            BigDecimal heightCm, String materials) {
    }

    public record ProductSpecResponse(UUID id, UUID productId, BigDecimal weightKg, BigDecimal lengthCm,
            BigDecimal widthCm, BigDecimal heightCm, String materials) {
        static ProductSpecResponse from(ProductSpec spec) {
            return new ProductSpecResponse(spec.id(), spec.productId(), spec.weightKg(), spec.lengthCm(), spec.widthCm(),
                    spec.heightCm(), spec.materials());
        }
    }

    public record CreateVariantRequest(@NotBlank String sku, String barcode, @NotBlank String label, boolean isDefault,
            String status) {
    }

    public record VariantResponse(UUID id, UUID productId, String sku, String barcode, String label, boolean isDefault,
            String status) {
        static VariantResponse from(Variant variant) {
            return new VariantResponse(variant.id(), variant.productId(), variant.sku(), variant.barcode(),
                    variant.label(), variant.isDefault(), variant.status());
        }
    }

    public record AddVariantAttributeRequest(@NotBlank String attributeName, @NotBlank String attributeValue) {
    }

    public record VariantAttributeResponse(UUID id, UUID variantId, String attributeName, String attributeValue) {
        static VariantAttributeResponse from(VariantAttribute attribute) {
            return new VariantAttributeResponse(attribute.id(), attribute.variantId(), attribute.attributeName(),
                    attribute.attributeValue());
        }
    }

    public record DefineVariantPriceRequest(@NotBlank String priceType,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank String currency,
            Instant effectiveFrom) {
    }

    public record VariantPriceResponse(UUID id, UUID variantId, String priceType, BigDecimal amount, String currency,
            Instant effectiveFrom) {
        static VariantPriceResponse from(VariantPrice price) {
            return new VariantPriceResponse(price.id(), price.variantId(), price.priceType(), price.amount(),
                    price.currency(), price.effectiveFrom());
        }
    }

    public record CreateBatchRequest(@NotBlank String lotNumber, LocalDate manufacturingDate, LocalDate expiryDate,
            @NotNull Integer quantity) {
    }

    public record BatchResponse(UUID id, UUID productId, String lotNumber, LocalDate manufacturingDate,
            LocalDate expiryDate, int quantity) {
        static BatchResponse from(Batch batch) {
            return new BatchResponse(batch.id(), batch.productId(), batch.lotNumber(), batch.manufacturingDate(),
                    batch.expiryDate(), batch.quantity());
        }
    }

    public record CreateMediaAssetRequest(@NotBlank String targetType, @NotNull UUID targetId, @NotNull UUID fileId,
            @NotBlank String mimeType, int position, String altText) {
    }

    public record MediaAssetResponse(UUID id, String targetType, UUID targetId, UUID fileId, String mimeType,
            int position, String altText) {
        static MediaAssetResponse from(MediaAsset mediaAsset) {
            return new MediaAssetResponse(mediaAsset.id(), mediaAsset.targetType(), mediaAsset.targetId(),
                    mediaAsset.fileId(), mediaAsset.mimeType(), mediaAsset.position(), mediaAsset.altText());
        }
    }
}
