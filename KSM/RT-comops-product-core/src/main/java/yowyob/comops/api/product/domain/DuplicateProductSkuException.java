package yowyob.comops.api.product.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class DuplicateProductSkuException extends DomainException {

    public DuplicateProductSkuException(String sku) {
        super("A product already exists with sku: " + sku);
    }
}
