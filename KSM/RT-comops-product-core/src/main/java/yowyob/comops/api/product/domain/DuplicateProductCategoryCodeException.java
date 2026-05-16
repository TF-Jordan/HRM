package yowyob.comops.api.product.domain;

public final class DuplicateProductCategoryCodeException extends RuntimeException {

    public DuplicateProductCategoryCodeException(String code) {
        super("Product category code already exists: " + code);
    }
}
