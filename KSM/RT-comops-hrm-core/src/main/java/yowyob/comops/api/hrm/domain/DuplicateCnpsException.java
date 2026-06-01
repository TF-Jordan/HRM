package yowyob.comops.api.hrm.domain;

import yowyob.comops.api.common.domain.DomainException;

public final class DuplicateCnpsException extends DomainException {
    public DuplicateCnpsException(String numCnps) {
        super("An employee with CNPS number already exists: " + numCnps);
    }
}
