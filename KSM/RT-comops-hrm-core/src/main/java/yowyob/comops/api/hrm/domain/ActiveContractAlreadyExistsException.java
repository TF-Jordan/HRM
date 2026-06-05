package yowyob.comops.api.hrm.domain;

import java.util.UUID;

import yowyob.comops.api.common.domain.DomainException;

/**
 * Raised when a contract creation would leave an employee with more than one ACTIVE
 * contract. The invariant "one active contract per employee" is enforced and not
 * negotiable: the existing active contract must be terminated or renewed first.
 */
public final class ActiveContractAlreadyExistsException extends DomainException {
    public ActiveContractAlreadyExistsException(UUID employeeId) {
        super("Employee already has an active contract: " + employeeId
                + ". Terminate or renew it before creating a new one.");
    }
}
