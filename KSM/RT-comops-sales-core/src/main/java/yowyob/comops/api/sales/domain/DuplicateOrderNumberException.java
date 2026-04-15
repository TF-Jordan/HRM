package yowyob.comops.api.sales.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class DuplicateOrderNumberException extends DomainException {

    public DuplicateOrderNumberException(String orderNumber) {
        super("A sales order already exists with number: " + orderNumber);
    }
}
