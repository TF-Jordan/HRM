package yowyob.comops.api.hrm.domain;

import yowyob.comops.api.common.domain.DomainException;

import java.math.BigDecimal;

public class InsufficientLeaveBalanceException extends DomainException {

    public InsufficientLeaveBalanceException(BigDecimal requested, BigDecimal available) {
        super("Insufficient leave balance: requested " + requested + " days but only " + available + " available.");
    }
}
