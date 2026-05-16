package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SocialDeclarationSpringDataRepository extends ReactiveCrudRepository<SocialDeclarationEntity, UUID> {

    Mono<SocialDeclarationEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<SocialDeclarationEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
