package yowyob.comops.api.inventory.domain;

import java.util.UUID;

public class ProductTransformationNotFoundException extends RuntimeException {

    public ProductTransformationNotFoundException(UUID transformationId) {
        super("Product transformation not found: " + transformationId);
    }
}
