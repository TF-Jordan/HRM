package yowyob.comops.api.product.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.product.domain.DuplicateBatchLotNumberException;
import yowyob.comops.api.product.domain.DuplicateProductCategoryCodeException;
import yowyob.comops.api.product.domain.DuplicateProductSkuException;
import yowyob.comops.api.product.domain.DuplicateVariantSkuException;
import yowyob.comops.api.product.domain.ProductNotFoundException;
import yowyob.comops.api.product.domain.ProductSearchUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {ProductController.class, ProductCatalogController.class, ProductStructureController.class})
public class ProductExceptionHandler {

    @ExceptionHandler(DuplicateProductSkuException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateSku(DuplicateProductSkuException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "PRODUCT_SKU_DUPLICATE"));
    }

    @ExceptionHandler(DuplicateProductCategoryCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateCategoryCode(DuplicateProductCategoryCodeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "PRODUCT_CATEGORY_CODE_DUPLICATE"));
    }

    @ExceptionHandler(DuplicateVariantSkuException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateVariantSku(DuplicateVariantSkuException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "PRODUCT_VARIANT_SKU_DUPLICATE"));
    }

    @ExceptionHandler(DuplicateBatchLotNumberException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateBatchLotNumber(DuplicateBatchLotNumberException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "PRODUCT_BATCH_LOT_DUPLICATE"));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ProductNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage(), "PRODUCT_NOT_FOUND"));
    }

    @ExceptionHandler(ProductSearchUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleSearchUnavailable(ProductSearchUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.failure(exception.getMessage(), "PRODUCT_SEARCH_UNAVAILABLE"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(exception.getMessage(), "PRODUCT_INVALID_REQUEST"));
    }
}
