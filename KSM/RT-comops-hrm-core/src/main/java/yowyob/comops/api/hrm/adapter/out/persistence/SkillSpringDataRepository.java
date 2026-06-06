package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SkillSpringDataRepository extends ReactiveCrudRepository<SkillEntity, UUID> {

    Mono<SkillEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<SkillEntity> findAllByTenantId(UUID tenantId);

    Mono<Boolean> existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name);
}
