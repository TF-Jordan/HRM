package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.SocialDeclaration;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageSocialDeclarationUseCase {

    Mono<SocialDeclaration> create(CreateSocialDeclarationCommand command);

    Mono<SocialDeclaration> generate(UUID id, UUID fichierId);

    Mono<SocialDeclaration> submit(UUID id);

    Mono<SocialDeclaration> acknowledge(UUID id);

    Mono<SocialDeclaration> get(UUID id);

    Flux<SocialDeclaration> listByOrganization(UUID orgId);
}
