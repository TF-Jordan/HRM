package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.EmployeePersonalInfo;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface EmployeePersonalInfoRepository {

    Mono<EmployeePersonalInfo> save(EmployeePersonalInfo info);

    /** Returns empty if no row exists yet for this employee. */
    Mono<EmployeePersonalInfo> findByEmployeeId(UUID tenantId, UUID employeeId);
}
