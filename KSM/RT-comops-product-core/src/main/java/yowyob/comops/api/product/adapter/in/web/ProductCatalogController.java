package yowyob.comops.api.product.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.product.application.port.in.ProductCatalogUseCase;
import yowyob.comops.api.product.domain.model.ProductCategory;
import yowyob.comops.api.product.domain.model.ProductPrice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'products:write')")
public class ProductCatalogController {

    private final ProductCatalogUseCase useCase;

    public ProductCatalogController(ProductCatalogUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/product-categories")
    public Mono<ResponseEntity<ApiResponse<ProductCategoryResponse>>> createCategory(
            @Valid @RequestBody Mono<CreateProductCategoryRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.createCategory(tuple.getT2().tenantId(), tuple.getT1().organizationId(),
                        tuple.getT1().code(), tuple.getT1().name(), tuple.getT1().parentCode(),
                        tuple.getT1().description()))
                .map(ProductCategoryResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Product category created.")));
    }

    @GetMapping("/product-categories")
    public Mono<ResponseEntity<ApiResponse<List<ProductCategoryResponse>>>> listCategories(
            @RequestParam("organizationId") UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.listCategories(context.tenantId(), organizationId)
                        .map(ProductCategoryResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Product categories retrieved.")));
    }

    @PostMapping("/products/{productId}/prices")
    public Mono<ResponseEntity<ApiResponse<ProductPriceResponse>>> definePrice(@PathVariable("productId") UUID productId,
            @Valid @RequestBody Mono<DefineProductPriceRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.definePrice(tuple.getT2().tenantId(), productId, tuple.getT1().priceType(),
                        tuple.getT1().amount(), tuple.getT1().currency(), tuple.getT1().effectiveFrom()))
                .map(ProductPriceResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Product price defined.")));
    }

    @GetMapping("/products/{productId}/prices")
    public Mono<ResponseEntity<ApiResponse<List<ProductPriceResponse>>>> listPrices(@PathVariable("productId") UUID productId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.listPrices(context.tenantId(), productId)
                        .map(ProductPriceResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Product prices retrieved.")));
    }

    @GetMapping("/products/{productId}/prices/effective")
    public Mono<ResponseEntity<ApiResponse<ProductPriceResponse>>> resolveEffectivePrice(
            @PathVariable("productId") UUID productId,
            @RequestParam("priceType") String priceType,
            @RequestParam(value = "at", required = false) Instant at) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.resolveEffectivePrice(context.tenantId(), productId, priceType, at))
                .map(ProductPriceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Effective product price retrieved.")));
    }

    public record CreateProductCategoryRequest(@NotNull UUID organizationId, @NotBlank String code, @NotBlank String name,
            String parentCode, String description) {
    }

    public record ProductCategoryResponse(UUID id, UUID organizationId, String code, String name, String parentCode,
            String description) {
        static ProductCategoryResponse from(ProductCategory category) {
            return new ProductCategoryResponse(category.id(), category.organizationId(), category.code(), category.name(),
                    category.parentCode(), category.description());
        }
    }

    public record DefineProductPriceRequest(@NotBlank String priceType,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank String currency,
            Instant effectiveFrom) {
    }

    public record ProductPriceResponse(UUID id, UUID productId, String priceType, BigDecimal amount, String currency,
            Instant effectiveFrom) {
        static ProductPriceResponse from(ProductPrice price) {
            return new ProductPriceResponse(price.id(), price.productId(), price.priceType(), price.amount(),
                    price.currency(), price.effectiveFrom());
        }
    }
}
