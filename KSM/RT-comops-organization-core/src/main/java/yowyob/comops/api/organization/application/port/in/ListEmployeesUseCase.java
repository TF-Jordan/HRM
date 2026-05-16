package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.EmployeeMembership;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListEmployeesUseCase {

    Flux<EmployeeMembership> listEmployees(UUID organizationId);
}
