package yowyob.comops.api.product.domain;

public final class DuplicateVariantSkuException extends RuntimeException {

    public DuplicateVariantSkuException(String sku) {
        super("Variant sku already exists: " + sku);
    }
}
