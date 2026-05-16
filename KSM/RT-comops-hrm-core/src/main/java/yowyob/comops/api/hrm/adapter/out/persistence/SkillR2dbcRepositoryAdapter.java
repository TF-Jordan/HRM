package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.SkillRepository;
import yowyob.comops.api.hrm.domain.model.Skill;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class SkillR2dbcRepositoryAdapter implements SkillRepository {

    private final SkillSpringDataRepository repository;

    public SkillR2dbcRepositoryAdapter(SkillSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Skill> save(Skill skill) {
        return repository.save(toEntity(skill)).map(this::toDomain);
    }

    @Override
    public Mono<Skill> findById(UUID tenantId, UUID skillId) {
        return repository.findByIdAndTenantId(skillId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<Skill> findAll(UUID tenantId) {
        return repository.findAllByTenantId(tenantId).map(this::toDomain);
    }

    private SkillEntity toEntity(Skill s) {
        return new SkillEntity(s.id(), s.tenantId(), s.createdAt(), s.updatedAt(),
                s.name(), s.categorie(), s.description());
    }

    private Skill toDomain(SkillEntity e) {
        return Skill.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.name(), e.categorie(), e.description());
    }
}
