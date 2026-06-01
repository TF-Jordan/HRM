package yowyob.comops.api.hrm.domain;

import yowyob.comops.api.common.domain.DomainException;

import java.util.UUID;

public final class ContractNotFoundException extends DomainException {
    public ContractNotFoundException(UUID contractId) {
        super("Contract not found: " + contractId);
    }
}
