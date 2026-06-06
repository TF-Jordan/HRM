package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.Skill;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SkillRepository {

    Mono<Skill> save(Skill skill);

    Mono<Skill> findById(UUID tenantId, UUID skillId);

    Flux<Skill> findAll(UUID tenantId);

    /** Case-insensitive existence check used to keep skill names unique per tenant. */
    Mono<Boolean> existsByNameIgnoreCase(UUID tenantId, String name);
}
