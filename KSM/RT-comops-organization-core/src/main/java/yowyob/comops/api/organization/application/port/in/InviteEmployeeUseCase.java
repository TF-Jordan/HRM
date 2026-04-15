package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.EmployeeMembership;
import reactor.core.publisher.Mono;

public interface InviteEmployeeUseCase {

    Mono<EmployeeMembership> invite(InviteEmployeeCommand command);
}
