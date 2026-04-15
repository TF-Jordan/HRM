package yowyob.comops.api.sales.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class SalesOrderNotFoundException extends DomainException {
    public SalesOrderNotFoundException(UUID orderId) { super("Sales order not found: " + orderId); }
}
