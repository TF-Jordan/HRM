package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.EmployeeSkill;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeSkillRepository {

    Mono<EmployeeSkill> save(EmployeeSkill employeeSkill);

    Flux<EmployeeSkill> findByEmployeeId(UUID tenantId, UUID employeeId);

    Flux<EmployeeSkill> findBySkillId(UUID tenantId, UUID skillId);

    /** Every employee↔skill mapping in the tenant — used for the cartographie view. */
    Flux<EmployeeSkill> findAll(UUID tenantId);
}
