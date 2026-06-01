package yowyob.comops.api.inventory.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class DuplicateStockMovementReferenceException extends DomainException {

    public DuplicateStockMovementReferenceException(String referenceNumber) {
        super("A stock movement already exists with reference: " + referenceNumber);
    }
}
