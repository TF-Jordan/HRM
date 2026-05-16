package yowyob.comops.api.hrm.application.service;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.hrm.application.port.in.CreateEmployeeSkillCommand;
import yowyob.comops.api.hrm.application.port.in.CreateSkillCommand;
import yowyob.comops.api.hrm.application.port.in.ManageSkillUseCase;
import yowyob.comops.api.hrm.application.port.out.EmployeeSkillRepository;
import yowyob.comops.api.hrm.application.port.out.SkillRepository;
import yowyob.comops.api.hrm.domain.model.EmployeeSkill;
import yowyob.comops.api.hrm.domain.model.Skill;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@Service
public class SkillService implements ManageSkillUseCase {

    private final SkillRepository skillRepository;
    private final EmployeeSkillRepository employeeSkillRepository;

    public SkillService(SkillRepository skillRepository, EmployeeSkillRepository employeeSkillRepository) {
        this.skillRepository = skillRepository;
        this.employeeSkillRepository = employeeSkillRepository;
    }

    @Override
    public Mono<Skill> createSkill(CreateSkillCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> {
                    Skill skill = Skill.create(ctx.tenantId(), command.name(),
                            command.categorie(), command.description());
                    return skillRepository.save(skill);
                });
    }

    @Override
    public Mono<Skill> getSkill(UUID skillId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> skillRepository.findById(ctx.tenantId(), skillId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Skill not found"))));
    }

    @Override
    public Flux<Skill> listSkills() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> skillRepository.findAll(ctx.tenantId()));
    }

    @Override
    public Mono<EmployeeSkill> createEmployeeSkill(CreateEmployeeSkillCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> {
                    EmployeeSkill es = EmployeeSkill.create(ctx.tenantId(), command.employeeId(),
                            command.skillId(), command.niveauActuel(), command.niveauAttendu(),
                            command.dateEvaluation());
                    return employeeSkillRepository.save(es);
                });
    }

    @Override
    public Flux<EmployeeSkill> listEmployeeSkillsByEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> employeeSkillRepository.findByEmployeeId(ctx.tenantId(), employeeId));
    }

    @Override
    public Flux<EmployeeSkill> listEmployeeSkillsBySkill(UUID skillId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> employeeSkillRepository.findBySkillId(ctx.tenantId(), skillId));
    }
}
