package yowyob.comops.api.product.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.product.application.port.in.CreateProductCommand;
import yowyob.comops.api.product.application.port.in.CreateProductUseCase;
import yowyob.comops.api.product.application.port.in.DeleteProductUseCase;
import yowyob.comops.api.product.application.port.in.GetProductUseCase;
import yowyob.comops.api.product.application.port.in.ListProductsUseCase;
import yowyob.comops.api.product.application.port.in.SearchProductsUseCase;
import yowyob.comops.api.product.application.port.in.UpdateProductCommand;
import yowyob.comops.api.product.application.port.in.UpdateProductUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/products")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'products:write')")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final SearchProductsUseCase searchProductsUseCase;

    public ProductController(CreateProductUseCase createProductUseCase,
            GetProductUseCase getProductUseCase,
            ListProductsUseCase listProductsUseCase,
            UpdateProductUseCase updateProductUseCase,
            DeleteProductUseCase deleteProductUseCase,
            SearchProductsUseCase searchProductsUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.searchProductsUseCase = searchProductsUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<ProductResponse>>> createProduct(@Valid @RequestBody Mono<CreateProductRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> createProductUseCase.createProduct(new CreateProductCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT1().organizationId(),
                        tuple.getT1().sku(),
                        tuple.getT1().name(),
                        tuple.getT1().familyCode(),
                        tuple.getT1().categoryCode(),
                        tuple.getT1().variantLabel(),
                        tuple.getT1().barcode(),
                        tuple.getT1().description(),
                        tuple.getT1().unitPrice(),
                        tuple.getT1().currency(),
                        tuple.getT1().status())))
                .map(ProductResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Product created.")));
    }

    @GetMapping("/{productId}")
    public Mono<ResponseEntity<ApiResponse<ProductResponse>>> getProduct(@PathVariable("productId") UUID productId) {
        return getProductUseCase.getProduct(productId)
                .map(ProductResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Product retrieved.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<ProductResponse>>>> listProducts(
            @RequestParam("organizationId") UUID organizationId,
            @RequestParam(value = "familyCode", required = false) String familyCode,
            @RequestParam(value = "status", required = false) String status) {
        return listProductsUseCase.listProducts(organizationId, familyCode, status)
                .map(ProductResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Products retrieved.")));
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<ApiResponse<List<ProductSearchResponse>>>> searchProducts(
            @RequestParam("organizationId") UUID organizationId,
            @RequestParam("q") String query,
            @RequestParam(value = "familyCode", required = false) String familyCode,
            @RequestParam(value = "status", required = false) String status) {
        return searchProductsUseCase.searchProducts(organizationId, query, familyCode, status)
                .map(ProductSearchResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Product search results retrieved.")));
    }

    @PatchMapping("/{productId}")
    public Mono<ResponseEntity<ApiResponse<ProductResponse>>> updateProduct(@PathVariable("productId") UUID productId,
            @Valid @RequestBody Mono<UpdateProductRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> updateProductUseCase.updateProduct(new UpdateProductCommand(
                        tuple.getT2().tenantId(),
                        productId,
                        tuple.getT1().organizationId(),
                        tuple.getT1().sku(),
                        tuple.getT1().name(),
                        tuple.getT1().familyCode(),
                        tuple.getT1().categoryCode(),
                        tuple.getT1().variantLabel(),
                        tuple.getT1().barcode(),
                        tuple.getT1().description(),
                        tuple.getT1().unitPrice(),
                        tuple.getT1().currency(),
                        tuple.getT1().status())))
                .map(ProductResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Product updated.")));
    }

    @DeleteMapping("/{productId}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteProduct(@PathVariable("productId") UUID productId) {
        return deleteProductUseCase.deleteProduct(productId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Product deleted.")));
    }
}
