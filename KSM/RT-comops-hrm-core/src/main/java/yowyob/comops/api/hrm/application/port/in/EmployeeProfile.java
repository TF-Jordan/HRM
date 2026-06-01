package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.application.port.out.ActorPort;
import yowyob.comops.api.hrm.domain.model.Employee;

/**
 * Read carrier merging an employee with its actor personal identity and the
 * resolved manager display name for the 360° profile view.
 */
public record EmployeeProfile(Employee employee, ActorPort.ActorInfo actor, String managerDisplayName) {
}
