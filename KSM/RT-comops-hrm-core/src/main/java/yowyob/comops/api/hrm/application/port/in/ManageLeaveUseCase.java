package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.LeaveRequest;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageLeaveUseCase {

    Mono<LeaveRequest> submitLeave(SubmitLeaveCommand command);

    Mono<LeaveRequest> approveLeave(UUID leaveRequestId);

    Mono<LeaveRequest> rejectLeave(UUID leaveRequestId, String commentaire);

    Mono<LeaveRequest> cancelLeave(UUID leaveRequestId);

    Mono<LeaveRequest> getLeaveRequest(UUID leaveRequestId);

    Flux<LeaveRequest> listLeavesByEmployee(UUID employeeId);

    Flux<LeaveRequest> listPendingLeaves(UUID organizationId, UUID agencyId);
}
