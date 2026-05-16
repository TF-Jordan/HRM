package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.EmployeeSkillRepository;
import yowyob.comops.api.hrm.domain.model.EmployeeSkill;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class EmployeeSkillR2dbcRepositoryAdapter implements EmployeeSkillRepository {

    private final EmployeeSkillSpringDataRepository repository;

    public EmployeeSkillR2dbcRepositoryAdapter(EmployeeSkillSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<EmployeeSkill> save(EmployeeSkill employeeSkill) {
        return repository.save(toEntity(employeeSkill)).map(this::toDomain);
    }

    @Override
    public Flux<EmployeeSkill> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Flux<EmployeeSkill> findBySkillId(UUID tenantId, UUID skillId) {
        return repository.findAllByTenantIdAndSkillId(tenantId, skillId).map(this::toDomain);
    }

    private EmployeeSkillEntity toEntity(EmployeeSkill es) {
        return new EmployeeSkillEntity(es.id(), es.tenantId(), es.createdAt(), es.updatedAt(),
                es.employeeId(), es.skillId(), es.niveauActuel(), es.niveauAttendu(),
                es.dateEvaluation());
    }

    private EmployeeSkill toDomain(EmployeeSkillEntity e) {
        return EmployeeSkill.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.employeeId(), e.skillId(), e.niveauActuel(), e.niveauAttendu(),
                e.dateEvaluation());
    }
}
