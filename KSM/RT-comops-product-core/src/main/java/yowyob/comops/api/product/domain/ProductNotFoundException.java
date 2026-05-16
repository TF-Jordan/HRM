package yowyob.comops.api.product.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public class ProductNotFoundException extends DomainException {

    public ProductNotFoundException(UUID productId) {
        super("Product " + productId + " was not found.");
    }
}
