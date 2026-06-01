package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.LeaveBalance;
import yowyob.comops.api.hrm.domain.model.LeaveType;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveBalanceRepository {

    Mono<LeaveBalance> save(LeaveBalance leaveBalance);

    Mono<LeaveBalance> findByEmployeeIdAndTypeAndAnnee(UUID tenantId, UUID employeeId, LeaveType type, int annee);

    Flux<LeaveBalance> findByEmployeeId(UUID tenantId, UUID employeeId);

    Flux<LeaveBalance> findByEmployeeIdAndAnnee(UUID tenantId, UUID employeeId, int annee);
}
