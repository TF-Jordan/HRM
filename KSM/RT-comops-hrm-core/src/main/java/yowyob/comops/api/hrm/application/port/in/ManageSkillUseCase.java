package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.EmployeeSkill;
import yowyob.comops.api.hrm.domain.model.Skill;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageSkillUseCase {

    Mono<Skill> createSkill(CreateSkillCommand command);

    Mono<Skill> getSkill(UUID skillId);

    Flux<Skill> listSkills();

    Mono<EmployeeSkill> createEmployeeSkill(CreateEmployeeSkillCommand command);

    Flux<EmployeeSkill> listEmployeeSkillsByEmployee(UUID employeeId);

    Flux<EmployeeSkill> listEmployeeSkillsBySkill(UUID skillId);

    /** Org-wide mapping list — used by the DRH cartographie / gap-analysis view. */
    Flux<EmployeeSkill> listAllEmployeeSkills(UUID organizationId);
}
