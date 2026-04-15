package yowyob.comops.api.sales.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public class InvalidSalesOrderStateException extends DomainException {

    public InvalidSalesOrderStateException(UUID orderId, String currentStatus, String expectedStatus) {
        super("Sales order " + orderId + " is in status " + currentStatus + " but expected " + expectedStatus + ".");
    }
}
