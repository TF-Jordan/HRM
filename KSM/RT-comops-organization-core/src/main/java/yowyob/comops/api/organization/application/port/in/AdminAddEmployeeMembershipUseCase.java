package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.EmployeeMembership;
import reactor.core.publisher.Mono;

public interface AdminAddEmployeeMembershipUseCase {

    Mono<EmployeeMembership> addMembership(AdminAddEmployeeMembershipCommand command);
}
