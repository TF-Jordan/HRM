package yowyob.comops.api.hrm.domain;

import yowyob.comops.api.common.domain.DomainException;

import java.util.UUID;

public final class EmployeeNotFoundException extends DomainException {
    public EmployeeNotFoundException(UUID employeeId) {
        super("Employee not found: " + employeeId);
    }
}
