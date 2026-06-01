package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.LeaveRequest;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveRequestRepository {

    Mono<LeaveRequest> save(LeaveRequest leaveRequest);

    Mono<LeaveRequest> findById(UUID tenantId, UUID leaveRequestId);

    Flux<LeaveRequest> findByEmployeeId(UUID tenantId, UUID employeeId);

    Flux<LeaveRequest> findPendingByOrganizationId(UUID tenantId, UUID organizationId);

    Flux<LeaveRequest> findPendingByOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);
}
