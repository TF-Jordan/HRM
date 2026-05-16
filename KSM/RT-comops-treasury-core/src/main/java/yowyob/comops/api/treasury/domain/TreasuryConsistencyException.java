package yowyob.comops.api.treasury.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class TreasuryConsistencyException extends DomainException {
    public TreasuryConsistencyException(String message) {
        super(message);
    }
}
