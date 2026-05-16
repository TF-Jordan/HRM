package yowyob.comops.api.product.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class ProductSearchUnavailableException extends DomainException {

    public ProductSearchUnavailableException() {
        super("Product search is unavailable.");
    }
}
