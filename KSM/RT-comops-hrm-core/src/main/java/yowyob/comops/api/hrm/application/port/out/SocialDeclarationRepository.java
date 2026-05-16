package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.SocialDeclaration;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SocialDeclarationRepository {

    Mono<SocialDeclaration> save(SocialDeclaration declaration);

    Mono<SocialDeclaration> findById(UUID tenantId, UUID id);

    Flux<SocialDeclaration> findByOrganizationId(UUID tenantId, UUID orgId);
}
